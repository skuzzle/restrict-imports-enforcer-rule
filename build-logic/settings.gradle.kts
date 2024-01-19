dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

// kind of a hack to reuse the build cache configuration for both the included plugin build as well as
// the main build.
apply(from = "conventions/src/main/kotlin/build-logic.build-cache-conventions.settings.gradle.kts")

rootProject.name="build-logic"

include("accept-tos")
include("conventions")
include("release-plugin")
include("verify-publication")
include("code-style")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
