import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    alias(libs.plugins.mavenExec)
}


// TODO: fix duplication (see publishing-conventions)
//
// This holds the enforcer rule as Gradle publishes it, and nothing else of consequence:
// it is the outer Maven's local repository, and invoker-settings.xml exposes it to the
// integration tests as the 'local.central' repository so they can resolve the rule under
// test. The artifacts those tests pull from Central land in a repository of their own,
// per cross-version leg - see below.
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

// ProjectDependency.getDependencyProject() was removed in Gradle 9, resolve the project by its path instead
val publishEnforcerRuleTask: Task? = project(projects.restrictImportsEnforcerRule.path)
    .tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

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

            dependsOn(downloadTask)

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

            // Every leg resolves into its own local repository, so a 3.8.1 leg can not
            // leave an artifact behind that a 3.9.11 leg then silently passes on. It sits
            // beside the task's output directory rather than inside it: outputs are opted
            // into the build cache above, and ~10MB of third-party artifacts per leg do
            // not belong in a cache entry.
            //
            // Nothing tracks this directory, so it is wiped here rather than left to
            // Gradle. A local repository that outlives the build is how one truncated
            // enforcer-api jar kept every 'success' scenario failing for days in a reused
            // CI workspace: a checksum policy only applies while an artifact is being
            // downloaded, so nothing ever rechecks what is already there.
            val invokerLocalRepo = layout.buildDirectory.dir("m2-$taskName/repository")
            doFirst("wipe the invoker's local repository") {
                invokerLocalRepo.get().asFile.deleteRecursively()
            }

            mavenDir = maven.mavenHome(mavenVersion)
            goals(setOf("verify"))
            options.showVersion(true)
            options.strictChecksums(true)
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
                    // One invoker JVM per core, not two. Each scenario forks a JVM, so
                    // 2C oversubscribes the agent, and the surplus threads mostly widen
                    // the window in which they contend for the same local repository -
                    // which is not safe for concurrent access across processes and can
                    // not be locked on mavenMin (see extraArtifacts in the pom).
                    "fromGradle.integration-test-threads" to "1C",
                    "fromGradle.localIntegrationTestRepo" to invokerLocalRepo.get().asFile.absolutePath
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
