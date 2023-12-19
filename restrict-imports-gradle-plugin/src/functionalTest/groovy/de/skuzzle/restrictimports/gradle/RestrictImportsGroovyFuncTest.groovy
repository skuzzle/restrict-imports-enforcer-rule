package de.skuzzle.restrictimports.gradle

import org.gradle.testkit.runner.TaskOutcome


class RestrictImportsGroovyFuncTest extends BaseRestrictsImportsFuncTest {

    @Override
    GradleDSL getDsl() {
        return GradleDSL.GROOVY
    }

    def "returns NO-SOURCE if no src directories exist"() {
        given:
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
        def result = run(":restrictImports")

        then:
        result.task(":defaultRestrictImports").outcome == TaskOutcome.NO_SOURCE
    }


    def "full RestrictImportsExtension configuration example"() {
        given:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            allowedImports = ["java.util.logging.Logger"]
            parallel = false
            includeCompileCode = false
            includeTestCode = false
            parseFullCompilationUnit = false
        }
        """.stripIndent(true)

        when:
        run(":restrictImports")

        then:
        noExceptionThrown()
    }

    def "detects simple banned import"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports")

        then:
        result.output.contains("Reason: Use slf4j for logging")
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.FAILED
    }

    def "detects simple banned import with custom RestrictImports task"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        tasks.create("customRestrictImports", de.skuzzle.restrictimports.gradle.RestrictImports) {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports")

        then:
        println result.output
        result.output.contains("Reason: Use slf4j for logging")
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":customRestrictImports").outcome == TaskOutcome.FAILED
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "detects banned import with group definition"() {
        given:
        javaClassWithImports(["java.util.ArrayList"], "de.skuzzle.enforcer.restrictimports")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            group {
                reason = "These classes should not be used!"
                basePackages = ["**"]
                bannedImports = ["java.util.*"]
            }
            group {
                reason = "This is a more specific group"
                basePackages = ["de.skuzzle.enforcer.restrictimports.*"]
                bannedImports = ["java.util.*"]
            }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports", "-s")

        then:
        result.output.contains("Reason: This is a more specific group")
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.FAILED
    }

    def "detects full qualified banned import use"() {
        given:
        javaClassWithImports(body: """\
        void test() {
            java.util.List list = new java.util.ArrayList<>();
        }
        """.stripIndent(true))

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            bannedImports = ["java.util.ArrayList"]
            parseFullCompilationUnit = true
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports")

        then:
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.FAILED
    }

    def "fallback to line-by-line parsing if full compilation parsing failed"() {
        given:
        javaClassWithImports(body: """\
        @Test
        void enumMap() {
            enum E {
                A, B;
            }
        }
        """.stripIndent(true))

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            bannedImports = ["java.util.ArrayList"]
            parseFullCompilationUnit = true
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        result.output.contains("Banned imports analysis completed with warnings. Results may be inaccurate!")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "detects simple banned import in test code"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"], "", "SampleClass", "test/java")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports")

        then:
        result.output.contains("Reason: Use slf4j for logging")
        result.output.contains("Banned imports detected in TEST code")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.FAILED
    }

    def "does not fail the build if failBuild = false"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            failBuild = false
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        result.output.contains("Reason: Use slf4j for logging")
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "does not fail the build if restrictImports.failBuild system property is false"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports", "-DrestrictImports.failBuild=false")

        then:
        result.output.contains("Reason: Use slf4j for logging")
        result.output.contains("Banned imports detected")
        result.output.contains("Analysis of 1 file took")
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "does not detect banned imports in compile code if respective flag is false"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            includeCompileCode = false
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        // TODO: would be NO_SORCE in a perfect world?
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "does not detect banned imports in test code if respective flag is false"() {
        given:
        javaClassWithImports(imports:  ["java.util.logging.Logger"], srcSet: "test/java")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            includeTestCode = false
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        // TODO: would be NO_SORCE in a perfect world?
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "ignores detected banned import if it is contained in allow list"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            allowedImports = ["java.util.logging.Logger"]
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "ignores banned import if it is found in excluded file"() {
        given:
        javaClassWithImports(["java.util.logging.Logger"])

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            reason = "Use slf4j for logging"
            bannedImports = ["java.util.logging.**"]
            exclusions = ["SampleClass"]
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "ignores banned inputs covered by not-fixables"() {
        given:
        javaClassWithImports(["java.util.ArrayList", "java.util.LinkedList", "java.util.Map"], "de.skuzzle.enforcer.restrictimports")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }

        restrictImports {
            bannedImports = ["java.util.*"]
            notFixable {
                in = "de.skuzzle.enforcer.restrictimports.SampleClass"
                allowedImports = ["java.util.ArrayList", "java.util.Map"]
                because = "Required by third-party API"
            }
            notFixable {
                in = "de.skuzzle.enforcer.restrictimports.SampleClass"
                allowedImports = ["java.util.LinkedList"]
            }
        }
        """.stripIndent(true)

        when:
        def result = run(":restrictImports")

        then:
        result.task(":defaultRestrictImports").outcome == TaskOutcome.SUCCESS
    }

    def "should support two RestrictImports tasks"() {
        given:
        javaClassWithImports(["java.util.ArrayList", "java.util.LinkedList", "java.util.Map"], "de.skuzzle.enforcer.restrictimports")

        and:
        buildFile << """\
        plugins {
            id("java")
            id("de.skuzzle.restrictimports")
        }
        def ri1 = tasks.create("ri1", de.skuzzle.restrictimports.gradle.RestrictImports) {
            bannedImports = ["java.util.*"]
        }
        def ri2 = tasks.create("ri2", de.skuzzle.restrictimports.gradle.RestrictImports) {
            bannedImports = ["java.util.*"]
        }
        """.stripIndent(true)

        when:
        def result = runAndFail(":restrictImports")

        then:
        result.output.contains("Banned imports detected:")
        result.task(":ri1").outcome == TaskOutcome.FAILED
        result.task(":ri2").outcome == TaskOutcome.FAILED
    }
}
