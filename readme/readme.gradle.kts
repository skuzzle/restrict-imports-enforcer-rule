import de.skuzzle.buildlogic.CopyAndFilterReadmeTask

plugins {
    id("build-logic.base")
    id("build-logic.release-lifecycle")
}

val generateReadmeAndReleaseNotes by tasks.registering(CopyAndFilterReadmeTask::class) {
    group = "documentation"
    description = "Copies the readme and release notes file into the root directory, replacing all placeholders"
    sourceDir.set(project.projectDir)
    targetDir.set(project.rootDir)
    replaceTokens = mapOf(
        "project.version" to project.version as String,
        "project.groupId" to project.group as String,
        "project.pluginId" to providers.gradleProperty("pluginId"),
        "version.junit" to libs.versions.junit5.get(),
        "version.enforcer-api.min" to libs.versions.enforcerMin.get(),
        "version.enforcer-api.max" to libs.versions.enforcerMax.get(),
        "github.user" to providers.gradleProperty("githubUser"),
        "github.name" to providers.gradleProperty("githubRepo")
    )
}

tasks.prepareRelease.configure { dependsOn(generateReadmeAndReleaseNotes) }
