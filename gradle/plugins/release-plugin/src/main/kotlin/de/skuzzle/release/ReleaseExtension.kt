package de.skuzzle.release

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

abstract class ReleaseExtension {
    companion object {
        const val NAME = "release"
    }

    @get:Input
    abstract val releaseVersion: Property<String>

    @get:Input
    abstract val dryRun: Property<Boolean>

    @get:Input
    abstract val verbose: Property<Boolean>

    @get:Input
    abstract val mainBranch: Property<String>

    @get:Input
    abstract val devBranch: Property<String>

    @get:Input
    abstract val githubRepoOwner: Property<String>

    @get:Input
    abstract val githubRepoName: Property<String>

    @get:Input
    abstract val githubReleaseToken: Property<String>

    @get:Input
    abstract val releaseNotesContent: Property<String>

    @get:Input
    abstract val mergeBranches: Property<Boolean>
}
