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

val coverageReportName = "testCodeCoverageReport"
reporting {
    reports {
        create<JacocoCoverageReport>(coverageReportName) {
            // The name of the test suite whose coverage data is aggregated, not the value of the
            // `testType` attribute that this replaced in Gradle 8.13: a name that no suite has
            // silently aggregates nothing, and the report task is SKIPPED for having no input.
            testSuiteName = "test"
        }
    }
}

coverallsJacoco {
    reportPath =
        "${layout.buildDirectory.asFile.get().absolutePath}/reports/jacoco/${coverageReportName}/${coverageReportName}.xml"
    // Restricts the upload to our own sources: the aggregated report also covers the
    // dependencies that the published artifacts shade into themselves.
    reportSourceSets = rootProject.allJavaModules()
        .flatMap { it.sourceSets["main"].allSource.srcDirs }
}

tasks.named("coverallsJacoco") {
    dependsOn(coverageReportName)
}
