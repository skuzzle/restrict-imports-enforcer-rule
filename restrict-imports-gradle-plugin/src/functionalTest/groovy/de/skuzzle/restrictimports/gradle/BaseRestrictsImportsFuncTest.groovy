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
        settingsFile = workspace.file("settings${dsl.fileExtension}")
        buildFile = workspace.file("build${dsl.fileExtension}")
    }

    BuildResult run(String... arguments) {
        return gradleRunner(arguments).build()
    }

    BuildResult runAndFail(String... arguments) {
        return gradleRunner(arguments).buildAndFail()
    }

    @NamedVariant
    def javaClassWithImports(List<String> imports = [], String packageName = "", String name = "SampleClass", String srcSet = "main/java", String body = "") {
        def path = workspace.resolve("src/$srcSet/${packageName.split("\\.").join(File.pathSeparator)}")
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
