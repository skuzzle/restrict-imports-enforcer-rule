package de.skuzzle.release

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Not worth caching")
abstract class AbstractReleaseStep() : DefaultTask() {

    @get:Inject
    abstract val providers: ProviderFactory

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

    @get:Internal
    val git: Git

    init {
        dryRun.convention(false)
        verbose.convention(true)
        git = Git(providers, dryRun, verbose)
    }

    fun print(s: String) {
        logger.lifecycle(s)
    }

    fun printOrVerbose(normal: String, verbose: String) {
        if (this.verbose.get()) print(verbose) else print(normal)
    }

    fun printVerbose(s: String) {
        if (verbose.get()) {
            logger.lifecycle(s)
        }
    }

}
