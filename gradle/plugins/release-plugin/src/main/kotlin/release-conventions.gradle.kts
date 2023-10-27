import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import de.skuzzle.release.FinalizeReleaseTask
import de.skuzzle.release.Git
import de.skuzzle.release.ReleaseExtension
import de.skuzzle.release.ReleaseLocalTask
import de.skuzzle.semantic.Version

plugins {
    id("com.github.breadmoirai.github-release")
}

require(project == rootProject) { "Release plugin should only be applied to root project" }

val releaseExtension = extensions.create<ReleaseExtension>(ReleaseExtension.NAME).apply {
    mainBranch.convention("main")
    devBranch.convention("dev")

    releaseVersion.convention(
        providers.systemProperty("RELEASE_VERSION")
            .orElse(providers.environmentVariable("RELEASE_VERSION"))
            .orElse(providers.gradleProperty("releaseVersion"))
    )

    dryRun.convention(
        providers.systemProperty("RELEASE_DRY_RUN").map { it == "true" }
            .orElse(providers.environmentVariable("RELEASE_DRY_RUN").map { it == "true" })
            .orElse(providers.gradleProperty("releaseDryRun").map { it == "true" })
            .orElse(false)
    )
    verbose.convention(
        providers.systemProperty("RELEASE_VERBOSE").map { it == "true" }
            .orElse(providers.environmentVariable("RELEASE_VERBOSE").map { it == "true" })
            .orElse(providers.gradleProperty("releaseVerbose").map { it == "true" })
            .orElse(false)
    )

    githubReleaseToken.convention(
        providers.systemProperty("RELEASE_GITHUB_TOKEN")
            .orElse(providers.environmentVariable("RELEASE_GITHUB_TOKEN"))
            .orElse(providers.gradleProperty("releaseGithubToken"))
            .orElse("<no token>")
    )
    githubRepoName.convention(
        providers.systemProperty("RELEASE_GITHUB_REPO")
            .orElse(providers.environmentVariable("RELEASE_GITHUB_REPO"))
            .orElse(providers.gradleProperty("releaseGithubRepo"))
    )
    githubRepoOwner.convention(
        providers.systemProperty("RELEASE_GITHUB_OWNER")
            .orElse(providers.environmentVariable("RELEASE_GITHUB_OWNER"))
            .orElse(providers.gradleProperty("releaseGithubOwner"))
    )
}

githubRelease {
    draft.set(true)
    token(releaseExtension.githubReleaseToken)
    owner.set(releaseExtension.githubRepoOwner)
    repo.set(releaseExtension.githubRepoName)
    dryRun.set(releaseExtension.dryRun)
    body.set(releaseExtension.releaseNotesContent)
    targetCommitish.set(releaseExtension.mainBranch)
}

val calculatedVersion = calculateVersion()
logger.lifecycle("The current version is: $calculatedVersion")
rootProject.allprojects { this.version = calculatedVersion }

fun calculateVersion(): String {
    val git = Git(providers, releaseExtension.dryRun, releaseExtension.verbose)
    val latestTagValue = git.lastReleaseTag()
    val latestVersion = latestTagValue.substring(1)
    return releaseExtension.releaseVersion
        .orElse(provider {
            Version.parseVersion(latestVersion)
                .nextPatch("${git.currentBranch()}-SNAPSHOT")
                .toString()
        })
        .get()
}

val releaseLocal by tasks.creating(ReleaseLocalTask::class.java) {
    fromExtension(releaseExtension)
}

val pushReleaseInternal by tasks.creating(FinalizeReleaseTask::class.java) {
    fromExtension(releaseExtension)
}

val releaseTasks = tasks.withType(GithubReleaseTask::class.java)

releaseTasks.configureEach {
    dependsOn(pushReleaseInternal)
}

val pushRelease by tasks.registering {
    dependsOn(pushReleaseInternal, releaseTasks)
}
