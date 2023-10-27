package de.skuzzle.release

import org.gradle.api.tasks.TaskAction

abstract class FinalizeReleaseTask : AbstractReleaseStep() {

    @TaskAction
    fun finalizeRelease() {
        val currentBranch = git.currentBranch()
        val mainBranch = mainBranch.get()
        val devBranch = devBranch.get()
        if (currentBranch != mainBranch) {
            val message =
                "Can not finalize release: expected to be on main branch '$mainBranch' but was: $currentBranch"
            if (dryRun.get()) {
                println(message)
            } else {
                throw IllegalStateException(message)
            }
        }

        println("Pushing release commit to $currentBranch")
        git.git("push", "origin", mainBranch)
        println("Pushing release tag")
        git.git("push", "--tags")
        println("Switching to dev branch")
        git.git("checkout", devBranch)
        println("Pushing dev branch")
        git.git("push", "origin", devBranch)
    }
}
