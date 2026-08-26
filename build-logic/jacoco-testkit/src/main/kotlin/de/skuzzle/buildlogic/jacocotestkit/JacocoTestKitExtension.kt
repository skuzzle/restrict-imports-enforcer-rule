package de.skuzzle.buildlogic.jacocotestkit

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

abstract class JacocoTestKitExtension @Inject constructor(objects: ObjectFactory) {
    companion object {
        const val NAME = "jacocoTestKit"
    }

    /**
     * Names of the test tasks whose forked builds record coverage, as added by [testTasks]. The
     * plugin instruments each one as it is added.
     */
    val instrumentedTaskNames: DomainObjectSet<String> = objects.domainObjectSet(String::class.java)

    /** The system properties under which the instrumented test tasks talk to their tests. */
    @get:Nested
    abstract val systemPropertyNames: SystemPropertyNames

    /** The resolved agent, set by the plugin. */
    abstract val agentClasspath: ConfigurableFileCollection

    fun testTasks(vararg tasks: TaskProvider<out Test>) {
        // Reading only the name keeps the tasks unrealized
        tasks.forEach { instrumentedTaskNames.add(it.name) }
    }

    fun systemPropertyNames(action: Action<in SystemPropertyNames>) {
        action.execute(systemPropertyNames)
    }

    /**
     * The `-javaagent` argument that makes a JVM append its coverage to [destinationFile]. This is
     * the only place that knows the agent's options, so that every forked JVM records the same way,
     * no matter who launches it.
     *
     * Concurrent JVMs may share a destination file: the agent takes an exclusive lock on it.
     */
    fun javaAgentArgument(destinationFile: Provider<RegularFile>): Provider<String> =
        destinationFile.map { file ->
            "-javaagent:${agentClasspath.singleFile.absolutePath}" +
                "=destfile=${file.asFile.absolutePath},append=true,dumponexit=true,jmx=false"
        }

    abstract class SystemPropertyNames {
        /**
         * The property under which an instrumented test task passes [javaAgentArgument] to its
         * tests, for them to hand to the JVM they fork.
         */
        abstract val javaAgentArgument: Property<String>
    }
}
