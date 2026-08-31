import com.github.dkorotych.gradle.maven.exec.MavenExec

plugins {
    id("build-logic.base")
    id("build-logic.maven-download")
    alias(libs.plugins.mavenExec)
}


// TODO: fix duplication (see publishing-conventions)
val m2Repository: Provider<Directory> = rootProject.layout.buildDirectory.dir("m2")

// ProjectDependency.getDependencyProject() was removed in Gradle 9, resolve the project by its path instead
val publishEnforcerRuleTask: Task? = project(projects.restrictImportsEnforcerRule.path)
    .tasks.findByName("publishMavenPublicationToLocalIntegrationTestsRepository")

val functionalTest = tasks.register("functionalTest") {
    group = "verification"
}

// TEMPORARY DIAGNOSTIC - remove once the CI func-test failures are understood.
//
// Every invoker sub-build on CI (Jenkins and GitHub Actions alike) dies with
// NoClassDefFoundError: org/apache/maven/enforcer/rule/api/EnforcerRuleError, while the
// plugin realm lists enforcer-api-3.6.1.jar under this very repository. A URLClassLoader
// silently skips a URL whose file is absent or unreadable, which produces exactly that
// error - so this dumps what is actually on disk, from inside the CI container, both
// before Maven starts and after it has failed.
fun dumpInvokerRepoState(label: String, repoRoot: File) {
    fun log(line: String) = println("[IT-DIAG] $line")

    log("=== $label ===")
    log("user.name=${System.getProperty("user.name")} user.home=${System.getProperty("user.home")}")
    log("repoRoot=$repoRoot exists=${repoRoot.isDirectory} totalFiles=${repoRoot.walkTopDown().count { it.isFile }}")

    listOf("org/apache/maven/enforcer", "org/apache/maven/plugins/maven-enforcer-plugin").forEach { subPath ->
        val dir = repoRoot.resolve(subPath)
        log("-- $subPath exists=${dir.isDirectory}")
        if (!dir.isDirectory) return@forEach

        dir.walkTopDown().filter { it.isFile }.sortedBy { it.path }.forEach { file ->
            val sha1 = runCatching {
                java.security.MessageDigest.getInstance("SHA-1")
                    .digest(file.readBytes())
                    .joinToString("") { byte -> "%02x".format(byte) }
            }.getOrElse { "unreadable: $it" }
            log("   ${file.length()}\t$sha1\treadable=${file.canRead()}\t${file.relativeTo(repoRoot)}")

            if (file.name.endsWith(".lastUpdated") || file.name == "_remote.repositories") {
                file.readLines().filterNot { it.startsWith("#") || it.isBlank() }.forEach { log("      $it") }
            }
            if (file.name.startsWith("enforcer-api-") && file.name.endsWith(".jar")) {
                val entries = runCatching {
                    java.util.zip.ZipFile(file).use { zip ->
                        zip.entries().asSequence().count { it.name.contains("EnforcerRuleError") }
                    }
                }
                log("      EnforcerRuleError entries=${entries.getOrElse { "ZIP ERROR: $it" }}")
            }
        }
    }
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

            // TEMPORARY DIAGNOSTIC - remove with dumpInvokerRepoState above.
            // The 'after' dump runs before the exit-code assertion added by
            // importMavenInvokerTestResults, so it is reached on a failing run too.
            val invokerRepo = m2Repository.map { it.dir("repository").asFile }
            doFirst("dump invoker repo state before") { dumpInvokerRepoState("$taskName BEFORE", invokerRepo.get()) }
            doLast("dump invoker repo state after") { dumpInvokerRepoState("$taskName AFTER", invokerRepo.get()) }

            mavenDir = maven.mavenHome(mavenVersion)
            goals(setOf("verify"))
            options.showVersion(true)
            define(
                mapOf(
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
