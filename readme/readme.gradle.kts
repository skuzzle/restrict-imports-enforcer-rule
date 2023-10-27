import de.skuzzle.buildlogic.CopyAndFilterReadmeTask

plugins {
    `base-conventions`
}

val generateReadmeAndReleaseNotes by tasks.creating(CopyAndFilterReadmeTask::class.java) {
    group = "documentation"
    description = "Copies the readme and release notes file into the root directory, replacing all placeholders"
    sourceDir.set(project.projectDir)
    targetDir.set(project.rootDir)
    replaceTokens = mapOf(
        "project.version" to project.version as String,
        "project.groupId" to project.group as String,
        "version.junit" to libs.versions.junit5.get(),
        "version.enforcer-api.min" to libs.versions.enforcerMin.get(),
        "version.enforcer-api.max" to libs.versions.enforcerMax.get(),
        "github.user" to project.property("githubUser") as String,
        "github.name" to project.property("githubRepo") as String
    )
}
