plugins {
    id("build-logic.base")
    id("jacoco-report-aggregation")
    alias(libs.plugins.coverallsJacoco)
}

dependencies {
    rootProject.allJavaModules()
        .map { it.path }
        .forEach { jacocoAggregation(project(it)) }
    // Has no sources of its own, but records coverage of the published enforcer rule
    jacocoAggregation(projects.restrictImportsEnforcerRuleMavenIt)
}

// Report name to the name of the test suite it aggregates
val reportsBySuiteName = mapOf(
    "testCodeCoverageReport" to "test",
    "functionalTestCodeCoverageReport" to "functionalTest",
    "mavenFunctionalTestCodeCoverageReport" to "mavenFunctionalTest",
)
reporting {
    reports {
        reportsBySuiteName.forEach { (reportName, suiteName) ->
            create<JacocoCoverageReport>(reportName) {
                testSuiteName = suiteName
            }
        }
    }
}

// A JacocoCoverageReport covers exactly one test suite, but coveralls consumes a single report:
// merge the execution data of all of them. The class and source directories are the same for every
// one - they come from the aggregated projects, not from the suite - so taking them from any single
// report covers all modules.
val suiteReports = reportsBySuiteName.keys.map { tasks.named<JacocoReport>(it) }
val allTestsReport = tasks.register<JacocoReport>("allTestsCodeCoverageReport") {
    group = "verification"
    description = "Aggregates the coverage of every test suite of all projects"

    suiteReports.forEach { report -> executionData.from(report.map { it.executionData }) }
    classDirectories.from(suiteReports.first().map { it.classDirectories })
    sourceDirectories.from(suiteReports.first().map { it.sourceDirectories })

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

coverallsJacoco {
    reportPath = allTestsReport.get().reports.xml.outputLocation.get().asFile.absolutePath
    // Restricts the upload to our own sources: the aggregated report also covers the
    // dependencies that the published artifacts shade into themselves.
    reportSourceSets = rootProject.allJavaModules()
        .flatMap { it.sourceSets["main"].allSource.srcDirs }
}

tasks.named("coverallsJacoco") {
    dependsOn(allTestsReport)
}
