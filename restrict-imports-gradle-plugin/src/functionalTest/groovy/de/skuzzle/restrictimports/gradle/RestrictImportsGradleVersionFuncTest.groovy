package de.skuzzle.restrictimports.gradle

import org.gradle.testkit.runner.TaskOutcome

class RestrictImportsGradleVersionFuncTest extends BaseRestrictsImportsFuncTest {

    @Override
    GradleDSL getDsl() {
        return GradleDSL.GROOVY
    }

    def "does not use deprecated Gradle API with Gradle #gradleVersion"() {
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
        gradleVersion << ["9.7.0"]
    }
}
