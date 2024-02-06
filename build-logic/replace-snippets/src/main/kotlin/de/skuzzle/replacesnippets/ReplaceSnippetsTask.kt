package de.skuzzle.replacesnippets

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*

@CacheableTask
abstract class ReplaceSnippetsTask : DefaultTask() {
// Include syntax
// [!snippet in src/main/java/foo/bar/Xyz.Java]

// Snippet Syntax (Java):
// // [snippet]
// code
// // [/snippet]

// Snippet Syntax (XML):
// <!-- [snippet] -->
// code
// <!-- [/snippet] -->

    @get:[InputFiles PathSensitive(PathSensitivity.RELATIVE) Optional SkipWhenEmpty]
    abstract val documents: ConfigurableFileCollection

    @get:[Input Optional]
    abstract val additionalTokens: MapProperty<String, Any?>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun replaceSnippets() {
        val processor = DocumentProcessor { project.layout.projectDirectory.file(it).asFile }
        val outDir = outputDirectory.asFile.get()

        documents.forEach { documentFile ->
            val documentText = documentFile.readText()
            val resultText = processor.process(documentText, additionalTokens.orElse(mapOf()).get())
            val resultFile = outDir.resolve(documentFile.name)
            resultFile.writeText(resultText)
        }
    }
}
