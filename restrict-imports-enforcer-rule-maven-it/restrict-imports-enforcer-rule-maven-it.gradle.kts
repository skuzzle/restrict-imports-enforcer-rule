import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    alias(libs.plugins.mavenExec)
}


// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

// Maven downloads into this repository, and Gradle does not track what it writes there:
// the publish task only ever removes its own outputs, so third-party artifacts survive
// into the next build. In a reused CI workspace that is forever - which is how a
// truncated enforcer-api jar kept every 'success' scenario failing for days, on a branch
// whose sources were fine. Wiping it before anything publishes into it makes every build
// start from the state a fresh workspace would have.
val cleanInvokerLocalRepo = tasks.register<Delete>("cleanInvokerLocalRepo") {
    description = "Deletes the Maven Invoker's local repository so no build inherits another build's artifacts"
    delete(m2Repository)
}

// ProjectDependency.getDependencyProject() was removed in Gradle 9, resolve the project by its path instead
val publishEnforcerRuleTask: Task? = project(projects.restrictImportsEnforcerRule.path)
    .tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

publishEnforcerRuleTask?.mustRunAfter(cleanInvokerLocalRepo)

val functionalTest = tasks.register("functionalTest") {
    group = "verification"
}

data class CrossVersionTest(val mavenVersion: Provider<String>, val enforcerVersion: Provider<String>)

val crossVersionTests = listOf(libs.versions.mavenMin, libs.versions.mavenMax)
    .flatMap { mavenVersion ->
        listOf(libs.versions.enforcerMin, libs.versions.enforcerMax)
            .map { enforcerVersion -> CrossVersionTest(mavenVersion, enforcerVersion) }
    }

val funcTestTasks = crossVersionTests
    .map { crossVersionTest ->
        val enforcerVersion = crossVersionTest.enforcerVersion.get()
        val mavenVersion = crossVersionTest.mavenVersion.get()

        val safeEnforcerVersion = enforcerVersion.replace(".", "_")
        val safeMavenVersion = mavenVersion.replace(".", "_")

        val downloadTask = maven.download(mavenVersion)

        val taskName = "funcTestMaven_${safeMavenVersion}_enforcer_${safeEnforcerVersion}"

        val taskOutputDir = layout.buildDirectory.dir(taskName)

        val funcTestTask = tasks.register<MavenExec>(taskName) {
            description = "Executes Maven Enforcer Plugin integration tests"
            group = "verification"
            notCompatibleWithConfigurationCache("Inherently not")

            dependsOn(downloadTask, cleanInvokerLocalRepo)

            val mavenExecTask = this

            publishEnforcerRuleTask?.let { publishTask ->
                mavenExecTask.dependsOn(publishTask)
                mavenExecTask.inputs.files(publishTask.outputs)
                mavenExecTask.inputs.files(publishTask.project.tasks.withType<JavaCompile>())
            }

            inputs.files(layout.projectDirectory.dir("src/it/maven"))
                .withPropertyName("mavenIntegrationTestSources")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(layout.projectDirectory.file("invoker-settings.xml"))
                .withPropertyName("invokerSettings")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.files(layout.projectDirectory.file("pom.xml"))
                .withPropertyName("mavenItPom")
                .withPathSensitivity(PathSensitivity.RELATIVE)
            outputs.dir(taskOutputDir)

            // Opt this task into the build cache. The MavenExec task type from the
            // 'com.github.dkorotych.gradle-maven-exec' plugin is not annotated
            // @CacheableTask, so Gradle treats it as not-worth-caching by default.
            // All inputs above use RELATIVE path sensitivity, and mavenDir/define
            // are already content-tracked by the plugin, so caching is safe for
            // same-machine rebuilds. Cross-machine cache hits may still miss due
            // to absolute paths baked into the Maven define map.
            outputs.cacheIf("inputs fully tracked with relative path sensitivity") { true }

            mavenDir = maven.mavenHome(mavenVersion)
            goals(setOf("verify"))
            options.showVersion(true)
            define(
                mapOf(
                    // Without this the outer Maven resolves into ~/.m2, which on the CI
                    // agent is a bind mount shared live with every concurrently running
                    // build - and invoker-settings.xml then exposes that directory to all
                    // 16 invoker JVMs as the 'local.central' repository. Pointing it at the
                    // project-local repository keeps the integration tests off it entirely.
                    "maven.repo.local" to m2Repository.get().dir("repository").asFile.absolutePath,
                    "revision" to project.version.toString(),
                    "fromGradle.test-id" to "maven.invoker.it.maven_$safeMavenVersion.enforcer_$safeEnforcerVersion",
                    "fromGradle.output-dir" to taskName,
                    "fromGradle.enforcer-api-version" to enforcerVersion,
                    "fromGradle.invoker-plugin-version" to libs.versions.invokerPlugin.get(),
                    "fromGradle.integration-test-threads" to "2C",
                    "fromGradle.localIntegrationTestRepo" to m2Repository.get().dir("repository").asFile.absolutePath
                )
            )
        }

        // Report the Maven Invoker results as tests in the Build Scan.
        importMavenInvokerTestResults(funcTestTask, taskOutputDir)

        funcTestTask
    }

funcTestTasks.forEach {
    functionalTest.configure { dependsOn(it) }
    tasks.check.configure { dependsOn(it) }
}
