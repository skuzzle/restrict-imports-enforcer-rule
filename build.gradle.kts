plugins {
    alias(libs.plugins.researchgate.release)
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.github.release)
    `base-conventions`
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

release {
    pushReleaseVersionBranch.set("master")
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
