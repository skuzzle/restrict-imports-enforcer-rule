import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    `base-conventions`
    alias(libs.plugins.mavenExec)
}

// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

val publishEnforcerRuleTask =
    projects.restrictImportsEnforcerRule.dependencyProject.tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

listOf(libs.versions.enforcerMin, libs.versions.enforcerMax)
    .map { it.get() }
    .forEach { enforcerVersion ->
        val safeVersion = enforcerVersion.replace(".", "_")
        tasks.create<MavenExec>("runMavenIntegrationTests_$safeVersion") {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            val mavenExecTask = this
            with(publishEnforcerRuleTask) {
                mavenExecTask.dependsOn(this)
                mavenExecTask.inputs.files(this?.outputs)
                mavenExecTask.inputs.files(this?.project?.tasks?.withType<JavaCompile>())
            }

            val outputDir = "test-against-$enforcerVersion"
            inputs.files(layout.projectDirectory.dir("src/it/maven"))
            inputs.files(layout.projectDirectory.file("invoker-settings.xml"))
            inputs.files(layout.projectDirectory.file("pom.xml"))
            outputs.dir(layout.buildDirectory.file(outputDir))

            goals(setOf("verify"))
            define(
                mapOf(
                    "revision" to project.version.toString(),
                    "fromGradle.test-id" to "maven.invoker.it._$safeVersion",
                    "fromGradle.output-dir" to outputDir,
                    "fromGradle.enforcer-api-version" to enforcerVersion,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath
                )
            )
        }
    }

tasks.check {
    dependsOn(tasks.withType<MavenExec>())
}
