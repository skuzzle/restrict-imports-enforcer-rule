import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher


fun String.execute(): Unit {
    try {
        require(!this.isEmpty()) { "Empty command" }
        val parts = splitCommand(this)
        System.out.println("execute: " + parts)
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(2, TimeUnit.MINUTES)
        val stdIn = proc.inputStream.bufferedReader().readText()
        val stdOut = proc.errorStream.bufferedReader().readText()
        System.out.println("out> $stdIn\nerr> $stdOut")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// from https://stackoverflow.com/a/366532
fun splitCommand(command: String): List<String> {
    val matchList: MutableList<String> = ArrayList()
    val regex = "[^\\s\"']+|\"([^\"]*)\"|'([^']*)'".toRegex().toPattern()
    val regexMatcher: Matcher = regex.matcher(command)
    while (regexMatcher.find()) {
        if (regexMatcher.group(1) != null) {
            // Add double-quoted string without the quotes
            matchList.add(regexMatcher.group(1))
        } else if (regexMatcher.group(2) != null) {
            // Add single-quoted string without the quotes
            matchList.add(regexMatcher.group(2))
        } else {
            // Add unquoted word
            matchList.add(regexMatcher.group())
        }
    }
    return matchList
}
