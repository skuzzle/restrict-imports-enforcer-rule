plugins {
    id("build-logic.base")
    id("jacoco-report-aggregation")
    alias(libs.plugins.coverallsJacoco)
}

dependencies {
    rootProject.allJavaModules()
        .map { it.path }
        .forEach { jacocoAggregation(project(it)) }
}

val unitTestReportName = "testCodeCoverageReport"
val functionalTestReportName = "functionalTestCodeCoverageReport"
reporting {
    reports {
        // The names of the test suites whose coverage data is aggregated, not the values of the
        // `testType` attribute that this replaced in Gradle 8.13: a name that no suite has
        // silently aggregates nothing, and the report task is SKIPPED for having no input.
        create<JacocoCoverageReport>(unitTestReportName) {
            testSuiteName = "test"
        }
        create<JacocoCoverageReport>(functionalTestReportName) {
            testSuiteName = "functionalTest"
        }
    }
}

// A JacocoCoverageReport covers exactly one test suite, but coveralls consumes a single report:
// merge the execution data of both aggregations. The class and source directories are the same
// for both - they come from the aggregated projects, not from the suite - so taking them from
// either report covers all modules.
val unitTestReport = tasks.named<JacocoReport>(unitTestReportName)
val functionalTestReport = tasks.named<JacocoReport>(functionalTestReportName)
val allTestsReport = tasks.register<JacocoReport>("allTestsCodeCoverageReport") {
    group = "verification"
    description = "Aggregates the unit and functional test coverage of all projects"

    executionData.from(unitTestReport.map { it.executionData })
    executionData.from(functionalTestReport.map { it.executionData })
    classDirectories.from(unitTestReport.map { it.classDirectories })
    sourceDirectories.from(unitTestReport.map { it.sourceDirectories })

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
