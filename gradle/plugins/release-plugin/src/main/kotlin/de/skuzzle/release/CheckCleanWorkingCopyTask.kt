package de.skuzzle.release

import org.gradle.api.tasks.TaskAction

abstract class CheckCleanWorkingCopyTask : AbstractReleaseStep() {

    @TaskAction
    fun finalizeRelease() {
        val status = git.status()
        if (status.isNotEmpty()) {
            throw IllegalStateException("Can not release: Working copy is not clean\n$status")
        }
    }
}
