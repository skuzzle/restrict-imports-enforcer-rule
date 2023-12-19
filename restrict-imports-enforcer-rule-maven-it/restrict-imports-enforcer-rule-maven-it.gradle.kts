import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    `base-conventions`
    alias(libs.plugins.mavenExec)
}

// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

listOf(libs.versions.enforcerMin, libs.versions.enforcerMax)
    .map { it.get() }
    .forEach {
        val safeVersion = it.replace(".", "_")
        tasks.create<MavenExec>("runMavenIntegrationTests_$safeVersion") {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            val mavenExecTask = this
            rootProject.subprojects.forEach { subproject ->
                val publishToTestRepoTask = subproject.tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")
                if (publishToTestRepoTask != null) {
                    mavenExecTask.dependsOn(publishToTestRepoTask)
                    mavenExecTask.inputs.files(publishToTestRepoTask.project.tasks.withType<JavaCompile>())
                }
            }

            val outputDir = "test-against-$it"
            outputs.dir(layout.buildDirectory.file(outputDir))

            goals(setOf("verify"))
            define(
                mapOf(
                    "revision" to project.version.toString(),
                    "fromGradle.test-id" to "maven.invoker.it._$safeVersion",
                    "fromGradle.output-dir" to outputDir,
                    "fromGradle.enforcer-api-version" to it,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath
                )
            )
        }
    }


tasks.named("check") { dependsOn(tasks.withType<MavenExec>()) }
