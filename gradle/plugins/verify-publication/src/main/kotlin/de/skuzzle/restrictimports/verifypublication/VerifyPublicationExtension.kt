package de.skuzzle.restrictimports.verifypublication

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal

abstract class VerifyPublicationExtension {
    companion object {
        val NAME = "verifyPublication"
    }

    @get:Internal
    val artifacts: MutableList<PublishedArtifactRule> = mutableListOf()

    @get:InputDirectory
    abstract val verificationRepoDir: DirectoryProperty

    fun expectPublishedArtifact(name: String, action: Action<PublishedArtifactRule>) {
        val pa = PublishedArtifactRule(name)
        action.execute(pa)
        artifacts.add(pa)
    }
}
