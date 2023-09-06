pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

plugins {
    id("settings-conventions")
}

var isCi = System.getenv("CI")?.toBoolean() ?: false
gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        isUploadInBackground = !isCi
        capture {
            isTaskInputFiles = true
        }
    }
}

rootProject.name = "restrict-imports"

include("restrict-imports-enforcer-rule-core")
include("restrict-imports-enforcer-rule")
include("restrict-imports-enforcer-rule-maven-it")

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
