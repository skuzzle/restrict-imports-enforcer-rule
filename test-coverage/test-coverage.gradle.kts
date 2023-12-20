plugins {
    id("build-logic.base")
    id("jacoco-report-aggregation")
    alias(libs.plugins.coveralls)
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
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

coveralls {
    sourceDirs = rootProject.allJavaModules()
        .flatMap { it.sourceSets["main"].allSource.srcDirs }
        .map { it.toString() }
    jacocoReportPath =
        "${layout.buildDirectory.asFile.get().absolutePath}/reports/jacoco/${coverageReportName}/${coverageReportName}.xml"
}

tasks.named("coveralls") {
    dependsOn(coverageReportName)
}
