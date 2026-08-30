package de.skuzzle.restrictimports.gradle

import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion

class RestrictImportsGradleVersionFuncTest extends BaseRestrictsImportsFuncTest {

    @Override
    GradleDSL getDsl() {
        return GradleDSL.GROOVY
    }

    @Override
    protected boolean instrumentTestKitDaemon() {
        // Gradle 8 fails a build that uses a Java agent together with the configuration cache, and
        // this test intentionally runs with the configuration cache on. The coverage lost here is
        // recorded by the tests that run against the current Gradle anyway.
        return false
    }

    def "plugin works without using deprecated Gradle API with Gradle #gradleVersion"() {
        given:
        javaClassWithImports([], "", "ClassWithNoBannedImports")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            bannedImports = ["java.util.logging.**"]
        }
        """.stripIndent(true)

        when:
        // --warning-mode=fail turns any deprecation warning into a build failure
        def result = runWithGradleVersion(gradleVersion, ":restrictImports", "--warning-mode=fail")

        then:
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS

        where:
        // latest release of every Gradle generation we support, plus the version this project builds with
        gradleVersion << ["7.6.6", "8.14.5", GradleVersion.current().version]
    }
}
