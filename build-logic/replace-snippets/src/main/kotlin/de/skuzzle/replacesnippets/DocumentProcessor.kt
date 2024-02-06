package de.skuzzle.replacesnippets

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.utils.`is`
import java.io.File
import java.io.IOException

class DocumentProcessor(private val fileResolver: SnippetFileResolver) {

    private val snippetReferenceRegex =
        "(?<filesnippet>\\[!(?<snippet>[A-z0-9+-]+) in (?<path>[^]]+)])|(?<mapsnippet>@(?<mapkey>[A-z0-9+-.]+)@)".toRegex()
    private val fileCache = mutableMapOf<File, String>()
    private val snippetCache = mutableMapOf<TagAndFile, String>()

    fun process(document: String, additionalTokens: Map<String, Any?>): String {
        return document.replace(snippetReferenceRegex) {
            if (it.groups["filesnippet"]?.value != null) {
                val snippet = it.groups["snippet"]!!.value
                val path = it.groups["path"]!!.value
                resolveFileSnippet(snippet, path)
            } else {
                val mapkey = it.groups["mapkey"]!!.value
                val rawValue = additionalTokens[mapkey]
                    ?: throw RuntimeException("Could not find tag '$mapkey' within additional tokens map: $additionalTokens")
                unwrapValue(rawValue)
            }
        }
    }

    private fun unwrapValue(value: Any?): String {
        return if (value == null) {
            throw RuntimeException("Unwrapping $value did not yield a result")
        } else if (value is Provider<*>) {
            unwrapValue(value.orNull)
        } else {
            value.toString()
        }
    }

    private fun resolveFileSnippet(tag: String, path: String): String {
        val asFile = fileResolver.resolveSnippetFile(path)
        val snippetKey = TagAndFile(tag, asFile)
        val cachedSnippet = snippetCache[snippetKey]
        if (cachedSnippet != null) {
            return cachedSnippet
        }
        val contents = fileCache.computeIfAbsent(asFile) { f -> tryRead(f, tag, path) }

        var snippet = "";
        var startTag = contents.indexOf("[$tag]")

        if (startTag < 0) {
            throw RuntimeException("Could not find snippet tag '$tag' in $asFile")
        }

        while (startTag >= 0) {
            val endTag = contents.indexOf("[/$tag]", startTag)
            if (endTag < 0) {
                throw RuntimeException("Could not find end tag  '$tag' in $asFile")
            }
            val substr = contents.substring(startTag, endTag)
            val lines = substr.lines()

            val snippetText = lines
                .filterIndexed { index, _ -> index > 0 && index < lines.size - 1 }
                .joinToString("\n")

            snippet += snippetText
            startTag = contents.indexOf("[$tag]", endTag)
            if (startTag >= 0) {
                snippet += "\n"
            }
        }
        snippetCache[snippetKey] = snippet
        return snippet
    }

    private fun endsWithNewLine(s: String) = s.endsWith("\n") || s.endsWith("\r\n") || s.endsWith("\r")

    private fun tryRead(file: File, tag: String, path: String): String {
        try {
            return file.readText()
        } catch (e: IOException) {
            throw IOException(
                "Could not read tag '$tag' from snippet file at '$path' (resolved to $file using $fileResolver)",
                e
            )
        }
    }

    data class TagAndFile(val tag: String, val file: File)

}
