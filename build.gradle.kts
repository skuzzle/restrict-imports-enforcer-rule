plugins {
    alias(libs.plugins.nexus.publish)
    id("build-logic.base")
    id("build-logic.release")
}

release {
    mainBranch = "master"
    devBranch = "develop"
    githubRepoOwner = "skuzzle"
    githubRepoName = "restrict-imports-enforcer-rule"
    releaseNotesContent = providers.fileContents(layout.projectDirectory.file("RELEASE_NOTES.md")).asText
}

nexusPublishing.repositories {
    sonatype {
        nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
        username = property("sonatype_USR").toString()
        password = property("sonatype_PSW").toString()
    }
}

tasks.prepareRelease.configure {
    val dryRunEnabled = release.dryRun.get()
    if (dryRunEnabled) {
        logger.lifecycle("Not promoting the staging repository because release dry run is enabled")
    } else {
        dependsOn(tasks.closeAndReleaseStagingRepositories)
    }
}

fun TaskContainer.connectIncludedBuildTasks(
    includedBuildName: String,
    taskName: String,
    taskToConnect: String = taskName,
) {
    named(taskName) {
        dependsOn(
            project.gradle.includedBuild(includedBuildName).task(":$taskToConnect")
        )
    }
}

tasks {
    connectIncludedBuildTasks("build-logic", "check")
    connectIncludedBuildTasks("build-logic", "quickCheck")
}
