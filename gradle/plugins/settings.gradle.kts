dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}

// kind of a hack to reuse the build cache configuration for both the included plugin build as well as
// the main build.
apply(from = "build-logic/src/main/kotlin/build-cache-conventions.settings.gradle.kts")

rootProject.name="plugins"

include("build-logic")
include("release-plugin")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
