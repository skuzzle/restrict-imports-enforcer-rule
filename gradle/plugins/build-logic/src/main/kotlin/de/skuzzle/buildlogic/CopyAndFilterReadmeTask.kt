package de.skuzzle.buildlogic

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.filter
import java.io.File
import javax.inject.Inject

abstract class CopyAndFilterReadmeTask : DefaultTask() {

    @get:Inject
    abstract val fileSystemOps: FileSystemOperations

    @get:Internal
    abstract val sourceDir: Property<File>

    @get:Internal
    abstract val targetDir: Property<File>

    @get:[Input Optional]
    abstract val replaceTokens: MapProperty<String?, Any?>

    @TaskAction
    fun copyAndFilter() {
        val tokens = "tokens" to flattenTokenMap()
        fileSystemOps.copy {
            from(sourceDir.get()) {
                include("*.md")
            }
            into(targetDir.get())
            filter(ReplaceTokens::class, tokens)
        }
    }

    private fun flattenTokenMap(): Map<String?, String?> {
        return replaceTokens.map { realMap -> realMap.mapValues { mapValue(it.value) } }.get()
    }

    private fun mapValue(value: Any?): String? {
        return if (value == null) {
            "<unknown>"
        } else if (value is Provider<*>) {
            mapValue(value.orNull)
        } else {
            value.toString()
        }
    }
}
