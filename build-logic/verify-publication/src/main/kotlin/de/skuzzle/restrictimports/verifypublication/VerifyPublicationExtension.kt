package de.skuzzle.restrictimports.verifypublication

import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

abstract class VerifyPublicationExtension {
    companion object {
        val NAME = "verifyPublication"
    }

    abstract val groupId: Property<String>
    val artifacts: MutableList<PublishedArtifactRule> = mutableListOf()
    abstract val verificationRepoDir: DirectoryProperty

    fun expectPublishedArtifact(name: String, action: Action<PublishedArtifactRule>) {
        val pa = PublishedArtifactRule(name)
        action.execute(pa)
        artifacts.add(pa)
    }
}
