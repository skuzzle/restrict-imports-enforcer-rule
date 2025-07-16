import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    alias(libs.plugins.mavenExec)
}


// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

val publishEnforcerRuleTask =
    projects.restrictImportsEnforcerRule.dependencyProject.tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

val functionalTest by tasks.registering {
    group = "verification"
}

data class CrossVersionTest(val mavenVersion: Provider<String>, val enforcerVersion: Provider<String>)

val crossVersionTests = listOf(libs.versions.mavenMin, libs.versions.mavenMax)
    .flatMap { mavenVersion ->
        listOf(libs.versions.enforcerMin, libs.versions.enforcerMax)
            .map { enforcerVersion -> CrossVersionTest(mavenVersion, enforcerVersion) }
    }

//crossVersionTests.forEach { maven.versions.add(it.mavenVersion) }

val funcTestTasks = crossVersionTests
    .map { crossVersionTest ->
        val enforcerVersion = crossVersionTest.enforcerVersion.get()
        val mavenVersion = crossVersionTest.mavenVersion.get()

        val safeEnforcerVersion = enforcerVersion.replace(".", "_")
        val safeMavenVersion = mavenVersion.replace(".", "_")

        val downloadTask = maven.download(mavenVersion)

        tasks.register<MavenExec>("funcTestMaven_${safeMavenVersion}_enforcer_${safeEnforcerVersion}") {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            dependsOn(downloadTask)

            val mavenExecTask = this

            with(publishEnforcerRuleTask) {
                mavenExecTask.dependsOn(this)
                mavenExecTask.inputs.files(this?.outputs)
                mavenExecTask.inputs.files(this?.project?.tasks?.withType<JavaCompile>())
            }

            val outputDir = mavenExecTask.name
            inputs.files(layout.projectDirectory.dir("src/it/maven"))
            inputs.files(layout.projectDirectory.file("invoker-settings.xml"))
            inputs.files(layout.projectDirectory.file("pom.xml"))
            outputs.dir(layout.buildDirectory.file(outputDir))

            mavenDir = maven.mavenHome(mavenVersion)
            goals(setOf("verify"))
            options.showVersion(true)
            define(
                mapOf(
                    "revision" to project.version.toString(),
                    "fromGradle.test-id" to "maven.invoker.it._$safeEnforcerVersion",
                    "fromGradle.output-dir" to outputDir,
                    "fromGradle.enforcer-api-version" to enforcerVersion,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath
                )
            )
        }
    }

funcTestTasks.forEach {
    functionalTest.configure { dependsOn(it) }
    tasks.check.configure { dependsOn(it) }
}
