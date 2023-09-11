import org.gradle.tooling.GradleConnector

plugins {
    alias(libs.plugins.researchgate.release)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.github.release)
    `base-conventions`
}

// HACK: This makes researchgate-release work with included build. See also: https://github.com/gradle/gradle/issues/8246
configure(listOf(tasks.release, tasks.runBuildTasks)) {
    configure {
        actions.clear()
        doLast {
            GradleConnector
                .newConnector()
                .forProjectDirectory(layout.projectDirectory.asFile)
                .connect()
                .use { projectConnection ->
                    val buildLauncher = projectConnection
                        .newBuild()
                        .forTasks(*tasks.toTypedArray())
                        .setStandardInput(System.`in`)
                        .setStandardOutput(System.out)
                        .setStandardError(System.err)
                    gradle.startParameter.excludedTaskNames.forEach {
                        buildLauncher.addArguments("-x", it)
                    }
                    buildLauncher.run()
                }
        }
    }
}

tasks.named("afterReleaseBuild").configure {
    dependsOn(provider {
        subprojects
            .filter { it.pluginManager.hasPlugin("publishing-conventions") }
            .map { it.tasks.named("publishToSonatype") }
    })
    dependsOn("addFilesToGit")
}

tasks.register("addFilesToGit") {
    onlyIf("not a snapshot version") { !project.isSnapshot }
    dependsOn()
    group = "release"
    description = "Commit changed/generated files during release"
    doLast {
        // NOTE: .execute() extension function defined in buildSrc
        "git add README.md RELEASE_NOTES.md".execute()
        "git add --force docs/*".execute()
    }
}

val releaseDryRun = System.getenv("RELEASE_DRY_RUN")?.toBoolean() ?: false
release {
    pushReleaseVersionBranch.set(if (releaseDryRun) null else "master")
    tagTemplate.set("v$version")
    git {
        requireBranch.set("develop")
    }
}

githubRelease {
    token(provider { property("ghToken") as String? })
    owner.set(property("githubUser").toString())
    repo.set(property("githubRepo").toString())
    draft.set(true)
    body(provider { file("RELEASE_NOTES.md").readText(Charsets.UTF_8) })
}

nexusPublishing.repositories {
    sonatype {
        username.set(property("sonatype_USR").toString())
        password.set(property("sonatype_PSW").toString())
    }
}
