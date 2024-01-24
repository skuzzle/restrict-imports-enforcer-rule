import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import de.skuzzle.release.FinalizeReleaseTask
import de.skuzzle.release.Git
import de.skuzzle.release.ReleaseExtension
import de.skuzzle.release.ReleaseGitLocalTask
import de.skuzzle.semantic.Version

plugins {
    id("build-logic.release-extension")
    id("com.github.breadmoirai.github-release")
}

require(project == rootProject) { "Release plugin should only be applied to root project" }

val releaseExtension = extensions.getByType(ReleaseExtension::class.java)

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
    git.fetchTags()
    val latestTagValue = git.lastReleaseTag()
    val latestVersion = latestTagValue.substring(1)
    return releaseExtension.releaseVersion
        .orElse(provider {
            Version.parseVersion(latestVersion)
                .nextPatch(git.currentBranch())
                .toString()
        })
        .get()
}

val releaseGitLocal by tasks.registering(ReleaseGitLocalTask::class) {
    fromExtension(releaseExtension)
}

val pushReleaseInternal by tasks.registering(FinalizeReleaseTask::class) {
    fromExtension(releaseExtension)
}

val githubReleaseTasks = tasks.withType(GithubReleaseTask::class.java)

githubReleaseTasks.configureEach {
    dependsOn(pushReleaseInternal)
}

val pushRelease by tasks.registering {
    dependsOn(pushReleaseInternal, githubReleaseTasks)
}
