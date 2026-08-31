import com.gradle.develocity.agent.gradle.test.ImportJUnitXmlReports
import com.gradle.develocity.agent.gradle.test.JUnitXmlDialect
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Reports the results of a Maven Invoker Plugin run as tests in the Build Scan.
 *
 * The invoker plugin already writes a JUnit XML report per integration test (see
 * `<writeJunitReport>` in the maven-it pom), so all that is needed is to hand those
 * reports to Develocity. This registers a finalizer of [invokerTask] which imports them
 * as real test executions, attributed to [invokerTask] itself.
 *
 * This is an extension on [Project] rather than on the invoker task: registering the
 * import task needs the `TaskContainer` at configuration time, which a `TaskProvider` can
 * only reach by realizing itself. The task is typed as [AbstractExecTask] and not as the
 * maven-exec plugin's `MavenExec`, which the conventions build does not depend on and
 * therefore can not see.
 *
 * @param invokerTask The task running the Maven Invoker Plugin.
 * @param invokerOutputDir That task's output directory.
 */
fun Project.importMavenInvokerTestResults(
    invokerTask: TaskProvider<out AbstractExecTask<*>>,
    invokerOutputDir: Provider<Directory>
) {
    // 'reports' must match <reportsDirectory> in the maven-it pom. Both directories live
    // inside the invoker task's output directory, so the normalized copies are part of
    // its build cache entry and survive a cache hit.
    val invokerReportsDir = invokerOutputDir.map { it.dir("reports") }
    val normalizedReportsDir = invokerOutputDir.map { it.dir("develocity-junit-reports") }

    invokerTask.configure {
        // Maven's exit code must not end the task before the reports are normalized
        // below: a failing action skips every action after it, and a run that failed is
        // precisely the run whose per-scenario reports are worth having in the Build
        // Scan. The exit code is asserted in the last action instead, so the task still
        // fails - and is therefore still not cached - for exactly the same runs as before.
        isIgnoreExitValue = true
        val invokerResult = executionResult

        doLast("normalize JUnit XML for Develocity") {
            normalizeInvokerReports(invokerReportsDir.get().asFile, normalizedReportsDir.get().asFile)
        }
        doLast("assert Maven Invoker exit code") {
            invokerResult.get().assertNormalExitValue()
        }
    }

    ImportJUnitXmlReports.register(tasks, invokerTask, JUnitXmlDialect.GENERIC).configure {
        // setFrom replaces the default, which is the whole output file tree of the
        // reference task: that would also feed the parser the invoker's own BUILD-*.xml
        // (a <build-job> format, not JUnit XML) and the cloned projects under builds/.
        reports.setFrom(normalizedReportsDir.map { it.asFileTree.matching { include("TEST-*.xml") } })
    }
}

/**
 * Writes copies of the invoker's JUnit XML reports that Develocity's parser accepts.
 *
 * The originals are left untouched, so the invoker's own reports stay as it wrote them.
 */
private fun normalizeInvokerReports(invokerReportsDir: File, normalizedReportsDir: File) {
    normalizedReportsDir.deleteRecursively()
    normalizedReportsDir.mkdirs()

    invokerReportsDir
        .listFiles { file -> file.name.startsWith("TEST-") && file.name.endsWith(".xml") }
        .orEmpty()
        .forEach { report ->
            normalizedReportsDir.resolve(report.name).writeText(withTimestamp(report))
        }
}

private val testSuiteDuration = Regex("<testsuite\\b[^>]*\\btime=\"([^\"]*)\"")

/**
 * Adds the 'timestamp' attribute that the Maven Invoker Plugin never writes on
 * `<testsuite>` (still true as of 3.10.1), but which Develocity's JUnit XML parser
 * requires, aborting the whole import without it.
 */
private fun withTimestamp(report: File): String {
    val xml = report.readText()
    // Only look at the opening <testsuite> tag: <system-out> embeds the whole Maven build
    // log, which may well contain 'timestamp=' itself.
    if (xml.substringBefore('>').contains("timestamp=")) {
        return xml
    }

    // The invoker writes each report once its build job has finished, so the file's
    // modification time minus the recorded duration approximates when that job started.
    // There is no better source: the XML records no start time.
    val durationMillis = testSuiteDuration.find(xml)
        ?.groupValues?.get(1)?.toDoubleOrNull()
        ?.times(1000)?.toLong()
        ?: 0L
    val startedAt = Instant.ofEpochMilli(report.lastModified()).minusMillis(durationMillis)
    val timestamp = OffsetDateTime.ofInstant(startedAt, ZoneId.systemDefault())
        .truncatedTo(ChronoUnit.SECONDS)

    return xml.replaceFirst("<testsuite ", "<testsuite timestamp=\"$timestamp\" ")
}
