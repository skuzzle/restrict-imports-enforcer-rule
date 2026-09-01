plugins {
    id("com.gradle.develocity") version "4.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    repositories {
        mavenLocal()
        gradlePluginPortal() // so that external plugins can be resolved in dependencies section
    }
}

val isCI = System.getenv("CI") != null

develocity {
    server = "https://community.develocity.cloud"
    // Must match the projectId of the main build: this is a separate build, so it needs its own
    // project association. Without it the instance treats its build cache requests as having no
    // associated project, which credentials that are scoped to the project may not read.
    projectId = "skuzzle"
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

rootProject.name="build-logic"

include("conventions")
include("release-plugin")
include("verify-publication")
include("code-style")
include("maven-download")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
