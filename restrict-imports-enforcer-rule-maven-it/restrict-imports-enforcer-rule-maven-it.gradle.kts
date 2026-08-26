import com.github.dkorotych.gradle.maven.exec.MavenExec
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.TestSuiteName
import org.gradle.api.attributes.VerificationType

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    id("build-logic.jacoco-testkit")
    alias(libs.plugins.mavenExec)
}

// This module has no sources of its own; it exercises the published enforcer rule through real
// Maven builds. The coverage those record is exposed exactly the way the `functionalTest` suite of
// a Java module would expose it, so that test-coverage's aggregation picks it up by suite name
// along with every other functional test of the build.
val coverageDataElements = configurations.consumable("coverageDataElementsForFunctionalTest") {
    description = "Binary results containing Jacoco test coverage of the Maven integration tests."
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(TestSuiteName.TEST_SUITE_NAME_ATTRIBUTE, objects.named("functionalTest"))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.JACOCO_RESULTS))
    }
}


// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

// ProjectDependency.getDependencyProject() was removed in Gradle 9, resolve the project by its path instead
val publishEnforcerRuleTask: Task? = project(projects.restrictImportsEnforcerRule.path)
    .tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

val functionalTest = tasks.register("functionalTest") {
    group = "verification"
}

data class CrossVersionTest(val mavenVersion: Provider<String>, val enforcerVersion: Provider<String>)

val crossVersionTests = listOf(libs.versions.mavenMin, libs.versions.mavenMax)
    .flatMap { mavenVersion ->
        listOf(libs.versions.enforcerMin, libs.versions.enforcerMax)
            .map { enforcerVersion -> CrossVersionTest(mavenVersion, enforcerVersion) }
    }

// Only the newest combination records coverage. The agent slows every JVM the invoker forks, and
// the invoker runs them in parallel against one local repository that starts out empty - which
// Maven only accesses safely from concurrent processes as of 3.9, so instrumenting `mavenMin` races
// it into downloading half a POM. Coverage is not lost by this: all four combinations put the same
// rule code through the same paths.
val instrumentedMavenVersion = libs.versions.mavenMax.get()
val instrumentedEnforcerVersion = libs.versions.enforcerMax.get()

val funcTestTasks = crossVersionTests
    .map { crossVersionTest ->
        val enforcerVersion = crossVersionTest.enforcerVersion.get()
        val mavenVersion = crossVersionTest.mavenVersion.get()

        val safeEnforcerVersion = enforcerVersion.replace(".", "_")
        val safeMavenVersion = mavenVersion.replace(".", "_")

        val downloadTask = maven.download(mavenVersion)

        val taskName = "funcTestMaven_${safeMavenVersion}_enforcer_${safeEnforcerVersion}"
        val recordsCoverage = mavenVersion == instrumentedMavenVersion &&
            enforcerVersion == instrumentedEnforcerVersion
        // Every Maven the invoker forks appends to this one file; the agent locks it
        val coverageData = layout.buildDirectory.file("jacoco/$taskName.exec")

        val funcTestTask = tasks.register<MavenExec>(taskName) {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            dependsOn(downloadTask)

            val mavenExecTask = this

            if (recordsCoverage) {
                outputs.file(coverageData).withPropertyName("coverageData")
            }

            publishEnforcerRuleTask?.let { publishTask ->
                mavenExecTask.dependsOn(publishTask)
                mavenExecTask.inputs.files(publishTask.outputs)
                mavenExecTask.inputs.files(publishTask.project.tasks.withType<JavaCompile>())
            }

            val outputDir = mavenExecTask.name
            inputs.files(layout.projectDirectory.dir("src/it/maven"))
                .withPropertyName("mavenIntegrationTestSources")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(layout.projectDirectory.file("invoker-settings.xml"))
                .withPropertyName("invokerSettings")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(layout.projectDirectory.file("pom.xml"))
                .withPropertyName("mavenItPom")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            outputs.dir(layout.buildDirectory.file(outputDir))

            // Opt this task into the build cache. The MavenExec task type from the
            // 'com.github.dkorotych.gradle-maven-exec' plugin is not annotated
            // @CacheableTask, so Gradle treats it as not-worth-caching by default.
            // All inputs above use RELATIVE path sensitivity, and mavenDir/define
            // are already content-tracked by the plugin, so caching is safe for
            // same-machine rebuilds. Cross-machine cache hits may still miss due
            // to absolute paths baked into the Maven define map.
            outputs.cacheIf("inputs fully tracked with relative path sensitivity") { true }

            mavenDir = maven.mavenHome(mavenVersion)
            goals(setOf("verify"))
            options.showVersion(true)
            define(
                mapOf(
                    "revision" to project.version.toString(),
                    "fromGradle.test-id" to "maven.invoker.it._$safeEnforcerVersion",
                    "fromGradle.output-dir" to outputDir,
                    "fromGradle.enforcer-api-version" to enforcerVersion,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath,
                    "fromGradle.maven-opts" to
                        if (recordsCoverage) jacocoTestKit.javaAgentArgument(coverageData).get() else ""
                )
            )
        }

        if (recordsCoverage) {
            artifacts.add(coverageDataElements.name, coverageData) {
                type = ArtifactTypeDefinition.BINARY_DATA_TYPE
                builtBy(funcTestTask)
            }
        }

        funcTestTask
    }

funcTestTasks.forEach {
    functionalTest.configure { dependsOn(it) }
    tasks.check.configure { dependsOn(it) }
}
