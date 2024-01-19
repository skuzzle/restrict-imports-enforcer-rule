package de.skuzzle.release

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class ReleaseExtension {
    companion object {
        const val NAME = "release"
    }

    abstract val releaseVersion: Property<String>

    abstract val dryRun: Property<Boolean>

    abstract val verbose: Property<Boolean>

    abstract val mainBranch: Property<String>

    abstract val devBranch: Property<String>

    abstract val githubRepoOwner: Property<String>

    abstract val githubRepoName: Property<String>

    abstract val githubReleaseToken: Property<String>

    abstract val releaseNotesContent: Property<String>

    abstract val mergeBranches: Property<Boolean>
}
