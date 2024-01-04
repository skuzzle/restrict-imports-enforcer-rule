package de.skuzzle.release

import de.skuzzle.semantic.Version
import org.gradle.api.tasks.TaskAction

abstract class ReleaseGitLocalTask : AbstractReleaseStep() {

    @Throws(IllegalStateException::class)
    @TaskAction
    fun release() {
        val releaseVersion = this.releaseVersion
            .orNull ?: throw IllegalStateException("Can not release: No -PreleaseVersion=x.y.z parameter specified")

        val parseError = tryParseVersion(releaseVersion)
        if (parseError != null) {
            throw IllegalStateException("Can not release: $releaseVersion is not a valid semantic version: ${parseError.message}")
        }

        val branch = git.currentBranch()
        print("Releasing $releaseVersion from branch $branch")
        print("Creating release commit & tag with ${git.status().lines().count()} modified files")
        git.git("add", ".")

        printVerbose("Creating release commit & tag")
        git.git("commit", "-m", "Release $releaseVersion")
        if (!dryRun.get()) {
            git.git("tag", "-a", "v${releaseVersion}", "-m", "Release $releaseVersion")
        }

        if (mergeBranches.get()) {
            print("Merging release into main branch")
            git.git("checkout", mainBranch.get())
            git.git("merge", "v${releaseVersion}", "--strategy-option", "theirs")
        }
    }

    private fun tryParseVersion(v: String): Exception? {
        return try {
            Version.parseVersion(v)
            null
        } catch (e: Exception) {
            e
        }
    }
}