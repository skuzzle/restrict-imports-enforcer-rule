package de.skuzzle.replacesnippets

import java.io.File

fun interface SnippetFileResolver {

    fun resolveSnippetFile(path : String) : File
}
