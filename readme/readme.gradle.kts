import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `base-conventions`
}

// Don't use 'Copy' Task here otherwise the main directory will become an output
tasks.register("generateReadmeAndReleaseNotes") {
    group = "documentation"
    description = "Copies the readme and release notes file into the root directory, replacing all placeholders"

    doLast {
        copy {
            from(project.projectDir) {
                include("*.md")
            }
            into(project.rootDir)
            filter(ReplaceTokens::class, "tokens" to mapOf(
                "project.version" to project.version as String,
                "project.groupId" to project.group as String,
                "version.junit" to libs.versions.junit5.get(),
                "version.enforcer-api.min" to libs.versions.enforcerMin.get(),
                "version.enforcer-api.max" to libs.versions.enforcerMax.get(),
                "github.user" to project.property("githubUser") as String,
                "github.name" to project.property("githubRepo") as String
            ))
        }
    }
}
