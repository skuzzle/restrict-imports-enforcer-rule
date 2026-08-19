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
base.archivesName = gradlePluginArtifactId
afterEvaluate {
    publishing.publications.named<MavenPublication>("pluginMaven") {
        artifactId = project.name
    }
}
gradlePlugin {
    website = "https://github.com/skuzzle/restrict-imports-enforcer-rule"
    vcsUrl = "https://github.com/skuzzle/restrict-imports-enforcer-rule"
    plugins.register("restrictImports") {
        id = providers.gradleProperty("pluginId").get()
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
            aFile("META-INF/services/de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport") {
                matching("") { content ->
                    content.contains("de.skuzzle.enforcer.restrictimports.parser.lang.KotlinGroovyLanguageSupport")
                }
            }
        }
    }
    expectPublishedArtifact("de.skuzzle.restrictimports.gradle.plugin") {
        withPomFileMatchingMavenCentralRequirements()
    }
}


// As of Gradle 9, gradleApi() is compiled to Java 17 bytecode, which a Java 8 compiler can not
// read. Compile with a Java 17 toolchain instead and let --release 8 take care of still producing
// Java 8 bytecode that is limited to the Java 8 API, so the plugin keeps working on Java 8.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
    options.release = 8
}

dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
}

val functionalTest = testing.suites.register<JvmTestSuite>("functionalTest") {
    useSpock(libs.versions.spock)

    dependencies {
        implementation(platform(libs.groovy.bom.get().toString()))
        implementation(libs.groovy.nio)
    }

    targets {
        all {
            testTask {
                // TestKit launches the Gradle distribution under test with the JVM that runs the
                // tests. Java 17 is the only version supported by every Gradle version we test
                // against: Gradle 9 requires at least 17 and Gradle 7.6 supports at most 19.
                javaLauncher = javaToolchains.launcherFor {
                    languageVersion = JavaLanguageVersion.of(17)
                }
            }
        }
    }
}

tasks.named<Task>("check") {
    dependsOn(functionalTest)
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
