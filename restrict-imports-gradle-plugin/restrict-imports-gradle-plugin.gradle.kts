plugins {
    alias(libs.plugins.gradlePluginPublish)
    `kotlin-dsl`
    groovy
    `jvm-test-suite`
    id("build-logic.published-java-component")
    id("build-logic.release-extension")
}

group = "de.skuzzle.restrictimports"
description = "Restrict Imports Gradle Plugin"

val gradlePluginArtifactId = "restrict-imports-gradle-plugin"
base.archivesName.set(gradlePluginArtifactId)
afterEvaluate {
    val pluginMaven by publishing.publications.getting(MavenPublication::class) {
        artifactId = project.name
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
        tags = setOf("codestyle", "imports")
    }
}

verifyPublication {
    groupId = "de.skuzzle.restrictimports"
    expectPublishedArtifact("restrict-imports-gradle-plugin") {
        withClassifiers("", "javadoc", "sources")
        // dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()
        withJarContaining {
            // Test for shadowed files
            aFile("de/skuzzle/enforcer/restrictimports/analyze/AnalyzeResult.class")
        }
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

tasks.publishPlugins.configure {
    val dryRunEnabled = release.dryRun.get()
    if (dryRunEnabled) {
        logger.info("Setting gradle plugin-publish to 'validate-only' because release dry run is enabled")
        setValidate(true)
    }
}
tasks.prepareRelease.configure { dependsOn(tasks.publishPlugins) }
