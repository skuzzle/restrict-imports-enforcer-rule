import de.skuzzle.buildlogic.jacocotestkit.JacocoTestKitExtension
import java.util.concurrent.Callable

plugins {
    id("jacoco")
}

// Some of our tests do not exercise the code under test in their own JVM: the Gradle plugin's
// functional tests drive a build through TestKit, which runs it in a Gradle daemon, and the Maven
// integration tests hand their projects to the Maven invoker, which forks a Maven per project. The
// agent that the `jacoco` plugin attaches records nothing of what happens in those JVMs.
//
// This plugin resolves a second agent and offers it as a `-javaagent` argument for such a JVM,
// which the test - the only place that knows how the JVM is launched - passes on. A test task
// hands it to its tests as a system property; other tasks can ask for it directly.

val extension = extensions.create<JacocoTestKitExtension>(JacocoTestKitExtension.NAME)
extension.systemPropertyNames.javaAgentArgument.convention("jacoco.agent.jvmarg")

val agentDependencies = configurations.dependencyScope("jacocoTestKitAgent")
val agentClasspath = configurations.resolvable("jacocoTestKitAgentClasspath") {
    extendsFrom(agentDependencies.get())
}
dependencies {
    // The `runtime` classifier artifact of the agent module *is* jacocoagent.jar
    agentDependencies.name("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime")
}
extension.agentClasspath.from(agentClasspath)

tasks.withType<Test>().configureEach {
    val instrumented = extension.testTasks.map { it.contains(name) }
    val propertyName = extension.systemPropertyNames.javaAgentArgument
    // The forked JVM has to append to this very file: it is the one the `jacoco` plugin registers
    // as the task's coverage data, and hence the only one an aggregated report picks up.
    val argument = extension.javaAgentArgument(
        layout.file(provider { the<JacocoTaskExtension>().destinationFile })
    )

    // Both of these are resolved after the configuration phase, so that this does not depend on
    // whether the task is realized before or after the build script configures the extension.
    inputs.files(Callable { if (instrumented.get()) agentClasspath else objects.fileCollection() })
        .withPropertyName("jacocoTestKitAgent")
        .withNormalizer(ClasspathNormalizer::class)
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        if (instrumented.get()) listOf("-D${propertyName.get()}=${argument.get()}") else emptyList()
    })
}
