@file:Suppress("UnstableApiUsage")

package de.skuzzle.release

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

class Git(
    private val providers: ProviderFactory,
    private val readOnly: Provider<Boolean>,
    private val debugOutput: Provider<Boolean>
) {

    private val writeOps = setOf("commit", "pull", "merge", "checkout", "push")

    fun status() = git("status", "--porcelain")

    fun currentBranch() = git("rev-parse", "--abbrev-ref", "HEAD")

    fun lastReleaseTag(): String {
        val latestTagHash = git("rev-list", "--tags", "--max-count=1")
        return git("describe", "--tags", latestTagHash, "--match=v[0-9]*")
    }

    private fun isReadonly() = readOnly.orElse(false).get()

    private fun isDebug() = debugOutput.orElse(false).get()

    fun git(vararg args: String): String {
        val fullArgs = listOf("git") + listOf(*args)
        val command = "\$ ${fullArgs.joinToString(" ")}"

        val mainCommand = args[0]
        if (isReadonly() && writeOps.contains(mainCommand)) {
            if (isDebug()) {
                println("$command\n> SKIPPED because readOnly mode is enabled")
            }
            return ""
        }

        val exec = providers.exec {
            this.commandLine(fullArgs)
            this.isIgnoreExitValue = true
        }
        val result = exec.result
            .orNull ?: throw IllegalStateException("No result while running: $command")

        val stdout = exec.standardOutput.asText.orElse("").get().trim()
        val stderr = exec.standardError.asText.orElse("").get().trim()
        val output = formatOutput(stdout, stderr)
        if (result.exitValue != 0) {
            throw IllegalStateException("Exit value ${result.exitValue} while running $command:\n$output")
        }

        if (isDebug()) {
            println("$command\n$output")
        }
        return stdout
    }

    private fun formatOutput(out: String, err: String): String {
        if (out.isNotEmpty() && err.isNotEmpty()) {
            return "out> $out\nerr> $err"
        } else if (out.isNotEmpty()) {
            return prependAll(out, "> ")
        } else if (err.isNotEmpty()) {
            return prependAll(err, "> ")
        }
        return ""
    }

    private fun prependAll(s: String, c: String): String {
        return s.lines().joinToString("\n") { c + it }
    }
}
