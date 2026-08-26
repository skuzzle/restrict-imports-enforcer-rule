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


// TestKit launches the Gradle distribution under test with the JVM that runs the tests, and
// Java 17 is the only version every Gradle version we test against supports: Gradle 9 requires at
// least 17 and Gradle 7.6 supports at most 19. This targets the whole module at that version so
// the functional tests are compiled for the JVM they are executed on. Production code is
// unaffected, build-logic.java-component compiles it with --release.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
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
}

// The functional tests exercise the plugin through TestKit, which runs the build under test in a
// Gradle daemon rather than in the test JVM. The JaCoCo agent that the `jacoco` plugin attaches to
// the test JVM therefore sees none of the plugin's code, so the tests have to attach a second agent
// to that daemon themselves - see BaseRestrictsImportsFuncTest. This configuration resolves the
// agent jar to attach; the `runtime` classifier artifact of the agent module *is* jacocoagent.jar.
val testKitJacocoAgent: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
}
dependencies {
    testKitJacocoAgent("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime")
}

tasks.named<Test>("functionalTest") {
    val agentJar = testKitJacocoAgent
    // The daemon has to append to this very file: it is the one the jacoco plugin registers as the
    // task's coverage data, and hence the only one that test-coverage's aggregation picks up.
    val execFile = the<JacocoTaskExtension>().destinationFile!!

    inputs.files(agentJar).withPropertyName("testKitJacocoAgent").withNormalizer(ClasspathNormalizer::class)
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Djacoco.agent.jar=${agentJar.singleFile.absolutePath}",
            "-Djacoco.agent.destfile=${execFile.absolutePath}",
        )
    })
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
