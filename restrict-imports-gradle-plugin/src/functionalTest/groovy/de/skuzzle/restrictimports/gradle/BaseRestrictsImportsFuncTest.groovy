package de.skuzzle.restrictimports.gradle

import groovy.transform.NamedVariant
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

abstract class BaseRestrictsImportsFuncTest extends Specification {

    @TempDir
    FileSystemFixture workspace
    def buildFile
    def settingsFile
    def propertiesFile

    abstract GradleDSL getDsl()

    def setup() {
        propertiesFile = workspace.file("gradle.properties")
        propertiesFile << """\
        org.gradle.parallel=true
        org.gradle.caching=true
        org.gradle.configuration-cache=true
        """.stripIndent(true)
        propertiesFile << jacocoDaemonProperties()
        settingsFile = workspace.file("settings${dsl.fileExtension}")
        buildFile = workspace.file("build${dsl.fileExtension}")
    }

    /**
     * Whether the Gradle daemon that runs the build under test should record coverage, see
     * {@link #jacocoDaemonProperties()}. Gradle 8 rejects a build that combines a Java agent with
     * the configuration cache, which these tests always enable, so tests that run against an older
     * Gradle have to turn this off.
     */
    protected boolean instrumentTestKitDaemon() {
        return true
    }

    /**
     * TestKit runs the build under test in a Gradle daemon, so the plugin's code never executes in
     * this JVM and the JaCoCo agent that the build attaches to it records nothing for the plugin.
     * Attach a second agent to that daemon, appending to the very file that the functionalTest task
     * declares as its execution data, so that the coverage of these tests ends up in the aggregated
     * report along with the unit tests'.
     *
     * <p>The daemon is single-use, which is what makes {@code dumponexit} write the data while the
     * test is still running rather than whenever a reused daemon happens to expire.
     *
     * <p>Yields nothing when the two system properties are absent, so that the tests still run when
     * they are started outside of the functionalTest task, e.g. from an IDE.
     */
    private String jacocoDaemonProperties() {
        def agentJar = System.getProperty("jacoco.agent.jar")
        def destFile = System.getProperty("jacoco.agent.destfile")
        if (!instrumentTestKitDaemon() || agentJar == null || destFile == null) {
            return ""
        }
        return """\
        org.gradle.daemon=false
        org.gradle.jvmargs=-javaagent:$agentJar=destfile=$destFile,append=true,dumponexit=true,jmx=false
        """.stripIndent(true)
    }

    BuildResult run(String... arguments) {
        return gradleRunner(arguments).build()
    }

    BuildResult runAndFail(String... arguments) {
        return gradleRunner(arguments).buildAndFail()
    }

    BuildResult runWithGradleVersion(String gradleVersion, String... arguments) {
        return gradleRunner(arguments).withGradleVersion(gradleVersion).build()
    }

    @NamedVariant
    def javaClassWithImports(List<String> imports = [], String packageName = "", String name = "SampleClass", String srcSet = "main/java", String body = "") {
        def path = workspace.resolve("src/$srcSet/${packageName.split("\\.").join(File.separator)}")
        def file = workspace.file("${path.toString()}/${name}.java")
        if (!packageName.empty) {
            file << "package $packageName;\n"
        }

        imports.each {
            file << "import $it;\n"
        }
        file << """class $name {
        $body
        }""".stripIndent(true)
    }

    private GradleRunner gradleRunner(String... arguments) {
        return GradleRunner.create()
            .withProjectDir(workspace.currentPath.toFile())
            .withArguments(arguments)
            .withPluginClasspath()
    }
}
