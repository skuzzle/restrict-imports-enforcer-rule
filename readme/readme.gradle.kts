import de.skuzzle.buildlogic.CopyAndFilterReadmeTask
import de.skuzzle.replacesnippets.ReplaceSnippetsTask

plugins {
    id("build-logic.base")
    id("build-logic.replace-snippets")
    id("build-logic.release-lifecycle")
}

val processedDir = layout.buildDirectory.dir("processedFiles")
val processReadmeAndReleaseNotes by tasks.registering(ReplaceSnippetsTask::class) {
    documents.from(layout.projectDirectory.files("README.md", "RELEASE_NOTES.md"))
    outputDirectory = processedDir
    additionalTokens = mapOf(
        "project.version" to project.version as String,
        "project.groupId" to project.group as String,
        "version.junit" to libs.versions.junit5,
        "version.enforcer-api.min" to libs.versions.enforcerMin,
        "version.enforcer-api.max" to libs.versions.enforcerMax,
        "github.user" to project.property("githubUser") as String,
        "github.name" to project.property("githubRepo") as String
    )
}

val copyReleaseNotesAndReadmeToRootDir by tasks.registering(CopyAndFilterReadmeTask::class) {
    group = "documentation"
    description = "Copies the readme and release notes file into the root directory, replacing all placeholders"
    dependsOn(processReadmeAndReleaseNotes)

    sourceDir = processedDir
    targetDir = project.rootDir
}

tasks.prepareRelease.configure { dependsOn(copyReleaseNotesAndReadmeToRootDir) }
