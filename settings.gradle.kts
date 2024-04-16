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

var isCi = !System.getenv("CI").isNullOrEmpty()
var acceptTos = isCi || file("YOU ACCEPTED THE TOS FOR PUBLISHING BUILD SCANS").exists()

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = if (acceptTos) "yes" else null
        uploadInBackground = !isCi
        capture {
            fileFingerprints = false
        }
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
