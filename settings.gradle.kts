pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

plugins {
    id("build-logic.settings-conventions")
}

val isCI = System.getenv("CI") != null
val isReleaseBuild = System.getenv("RELEASE_BUILD") != null
// Mirrors how the release plugin resolves its own dry run flag
val isDryRunRelease = (System.getProperty("RELEASE_DRY_RUN") ?: System.getenv("RELEASE_DRY_RUN")) == "true"

develocity {
    server = "https://community.develocity.cloud"
    projectId = "skuzzle"
    buildScan {
        if (isReleaseBuild) {
            tag("release-build")
            if (isDryRunRelease) {
                tag("release-dry-run")
            }
        }
        uploadInBackground = !isCI
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0" } }
        }
    }
}

buildCache {
    local {
        isEnabled = true
    }

    remote(develocity.buildCache) {
        isEnabled = true
        val accessKey = System.getenv("DEVELOCITY_ACCESS_KEY")
        isPush = isCI && accessKey != null
    }
}

rootProject.name = "restrict-imports"

include("restrict-imports-enforcer-rule-core")
include("restrict-imports-enforcer-rule")
include("restrict-imports-enforcer-rule-maven-it")
include("restrict-imports-gradle-plugin")

include("readme")
include("test-coverage")

// check that every subproject has a custom build file
// based on the project name
rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle"
    if (!project.buildFile.isFile) {
        project.buildFileName = "${project.name}.gradle.kts"
    }
    require(project.buildFile.isFile) {
        "${project.buildFile} must exist"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
