plugins {
    alias(libs.plugins.gradlePluginPublish)
    `kotlin-dsl`
    groovy
    `jvm-test-suite`
    id("published-java-component")
    id("verify-publication-conventions")
}

group = "de.skuzzle.restrictimports"
description = "Restrict Imports Gradle Plugin"
extra.apply {
    set("automaticModuleName", "de.skuzzle.restrictimports.gradle")
}

afterEvaluate {
    val pluginMaven by publishing.publications.getting(MavenPublication::class) {
        //artifactId = project.name
    }
}
gradlePlugin {
    website = "https://github.com/skuzzle/restrict-imports-enforcer-rule"
    vcsUrl = "https://github.com/skuzzle/restrict-imports-enforcer-rule"
    val restrictImports by plugins.creating {
        id = "de.skuzzle.restrictimports"
        implementationClass = "de.skuzzle.restrictimports.gradle.RestrictImportsPlugin"
        displayName = "Restrict Imports Gradle Plugin"
        description = project.description
    }
}

verifyPublication {
    expectPublishedArtifact("restrict-imports-gradle-plugin") {
        withClassifiers("", "javadoc", "sources")
        // dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()
    }
    expectPublishedArtifact("de.skuzzle.restrictimports.gradle.plugin") {
        withPomFileMatchingMavenCentralRequirements()
    }
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(project())
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
            }
        }

        tasks.named<Task>("check") {
            dependsOn(functionalTest)
        }
    }
}
gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
