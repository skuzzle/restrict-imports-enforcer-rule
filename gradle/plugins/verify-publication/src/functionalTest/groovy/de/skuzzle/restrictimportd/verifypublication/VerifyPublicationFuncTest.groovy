package de.skuzzle.restrictimportd.verifypublication

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class VerifyPublicationFuncTest extends Specification {
    @TempDir
    FileSystemFixture workspace
    def buildFile
    def settingsFile
    def propertiesFile

    def setup() {
        propertiesFile = workspace.file("gradle.properties")
        propertiesFile << """\
        group = org.gradle.sample
        version = 1.1
        org.gradle.parallel=true
        org.gradle.caching=true
        org.gradle.configuration-cache=true
        """.stripIndent(true)
        settingsFile = workspace.file("settings.gradle.kts")
        settingsFile << """\
        rootProject.name = "library"
        """.stripIndent(true)

        buildFile = workspace.file("build.gradle.kts")
        buildFile << """\
        plugins {
            id("maven-publish")
            id("java-library")
            id("verify-publication-conventions")
        }
        """.stripIndent(true)
    }

    def "accepts valid maven central pom"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("library") {
                withClassifiers("", "sources", "javadoc")
                withPomFileMatchingMavenCentralRequirements()
            }
        }
        """.stripIndent(true)
        buildFile << publishSourcesAndJavadoc()

        when:
        def result = run("verifyPublication")

        then:
        result.task(":verifyPublication").outcome == SUCCESS
    }

    def "fails if more artifacts than expected were published"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication { }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.output.contains("Expected 0 published artifacts but found 1")
        result.task(":verifyPublication").outcome == FAILED
    }

    def "fails if expected artifact wasn't published"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("not-published-artifact") { }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.task(":verifyPublication").outcome == FAILED
    }

    def "fails if pom does not have expected content"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("library") {
                withPomFileContentMatching("expected foo") { content -> content.contains("foo") }
            }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.output.contains("library-1.1.pom: content doesn't match: expected foo")
        result.task(":verifyPublication").outcome == FAILED
    }

    def "fails if expected file in jar is missing"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("library") {
                withJarContaining {
                    aFile("does/not/exist/in/jar")
                }
            }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.output.contains("library-1.1.jar does not contain expected file 'does/not/exist/in/jar")
        result.task(":verifyPublication").outcome == FAILED
    }

    def "fails if file in jar has not expected content"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("library") {
                withJarContaining {
                    aFile("META-INF/MANIFEST.MF") {
                        matching("Expected foo") { content -> content.contains("foo") }
                    }
                }
            }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.output.contains("META-INF/MANIFEST.MF: content doesn't match: Expected foo")
        result.task(":verifyPublication").outcome == FAILED
    }

    def "fails if expected classifier is missing"() {
        given:
        buildFile << publicationWithPom(validMavenCentralPom())
        buildFile << """\
        verifyPublication {
            expectPublishedArtifact("library") {
                withClassifiers("", "sources")
            }
        }
        """.stripIndent(true)

        when:
        def result = runAndFail("verifyPublication")

        then:
        result.output.contains("Expected a jar with classifier 'sources'")
        result.task(":verifyPublication").outcome == FAILED
    }

    private BuildResult run(String... arguments) {
        return gradleRunner(arguments).build()
    }

    private BuildResult runAndFail(String... arguments) {
        return gradleRunner(arguments).buildAndFail()
    }

    private GradleRunner gradleRunner(String... arguments) {
        return GradleRunner.create()
            .withProjectDir(workspace.currentPath.toFile())
            .withArguments(arguments)
            .withPluginClasspath()
    }

    private String publicationWithPom(String pom) {
        return """\
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    ${pom}
                }
            }
        }
        """.stripIndent(true)
    }

    private String validMavenCentralPom() {
        return """\
        pom {
            name = "Test Name"
            description = "Project description"
            url = "https://does.not.matter"
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            scm {
                connection.set("scm:git:git://does.not.matter.git")
                developerConnection.set("scm:git:ssh://git@doesnot.matter.git")
                url.set("https://does.not.matter")
            }
        }
        """.stripIndent(true)
    }

    private publishSourcesAndJavadoc() {
        return """\
        java {
            withJavadocJar()
            withSourcesJar()
        }
        """.stripIndent(true)
    }

}
