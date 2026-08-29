import com.github.dkorotych.gradle.maven.exec.MavenExec
import com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports
import com.gradle.develocity.agent.gradle.test.JUnitXmlDialect
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    alias(libs.plugins.mavenExec)
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

val funcTestTasks = crossVersionTests
    .map { crossVersionTest ->
        val enforcerVersion = crossVersionTest.enforcerVersion.get()
        val mavenVersion = crossVersionTest.mavenVersion.get()

        val safeEnforcerVersion = enforcerVersion.replace(".", "_")
        val safeMavenVersion = mavenVersion.replace(".", "_")

        val downloadTask = maven.download(mavenVersion)

        val taskName = "funcTestMaven_${safeMavenVersion}_enforcer_${safeEnforcerVersion}"

        // JUnit XML as written by the Maven Invoker Plugin, and the copies of it that are
        // normalized for Develocity (see the doLast block below).
        val invokerReportsDir = layout.buildDirectory.dir("$taskName/reports")
        val develocityReportsDir = layout.buildDirectory.dir("$taskName/develocity-junit-reports")

        val funcTestTask = tasks.register<MavenExec>(taskName) {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            dependsOn(downloadTask)

            val mavenExecTask = this

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
                    "fromGradle.test-id" to "maven.invoker.it.maven_$safeMavenVersion.enforcer_$safeEnforcerVersion",
                    "fromGradle.output-dir" to outputDir,
                    "fromGradle.enforcer-api-version" to enforcerVersion,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath
                )
            )

            doLast("normalize JUnit XML for Develocity") {
                // The Maven Invoker Plugin never writes the 'timestamp' attribute on
                // <testsuite> (still true as of 3.10.1), but Develocity's JUnit XML parser
                // requires it and aborts the whole import without it. Write normalized
                // copies rather than editing the originals, so the invoker's own reports
                // stay untouched. Both directories live inside this task's output
                // directory, so the copies survive a build cache hit as well.
                val reportsDir = invokerReportsDir.get().asFile
                val normalizedDir = develocityReportsDir.get().asFile
                normalizedDir.deleteRecursively()
                normalizedDir.mkdirs()

                // The invoker writes each report once its build job has finished, so the
                // file's modification time minus the recorded duration approximates when
                // that job started. There is no better source: the XML records no start.
                val durationPattern = Regex("<testsuite\\b[^>]*\\btime=\"([^\"]*)\"")

                reportsDir.listFiles { file -> file.name.startsWith("TEST-") && file.name.endsWith(".xml") }
                    .orEmpty()
                    .forEach { report ->
                        val xml = report.readText()
                        // Only look at the opening <testsuite> tag: <system-out> embeds the
                        // whole Maven build log, which may well contain 'timestamp=' itself.
                        val normalized = if (xml.substringBefore('>').contains("timestamp=")) {
                            xml
                        } else {
                            val durationMillis = durationPattern.find(xml)
                                ?.groupValues?.get(1)?.toDoubleOrNull()
                                ?.times(1000)?.toLong()
                                ?: 0L
                            val startedAt = Instant.ofEpochMilli(report.lastModified())
                                .minusMillis(durationMillis)
                            val timestamp = OffsetDateTime.ofInstant(startedAt, ZoneId.systemDefault())
                                .truncatedTo(ChronoUnit.SECONDS)
                            xml.replaceFirst("<testsuite ", """<testsuite timestamp="$timestamp" """)
                        }
                        normalizedDir.resolve(report.name).writeText(normalized)
                    }
            }
        }

        // Report the Maven Invoker results as tests in the Build Scan. The invoker plugin
        // already writes JUnit XML (see <writeJunitReport> in pom.xml); this registers a
        // finalizer task that hands those reports to Develocity, attributed to the
        // MavenExec task that produced them.
        ImportJUnitXmlReports.register(tasks, funcTestTask, JUnitXmlDialect.GENERIC).configure {
            // setFrom replaces the default, which is the task's entire output file tree:
            // that would also feed the parser the invoker's own BUILD-*.xml (a <build-job>
            // format, not JUnit XML) and the cloned projects under builds/.
            reports.setFrom(develocityReportsDir.map { it.asFileTree.matching { include("TEST-*.xml") } })
        }

        funcTestTask
    }

funcTestTasks.forEach {
    functionalTest.configure { dependsOn(it) }
    tasks.check.configure { dependsOn(it) }
}
