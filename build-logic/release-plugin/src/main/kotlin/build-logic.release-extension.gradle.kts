import de.skuzzle.release.ReleaseExtension
import org.gradle.kotlin.dsl.create

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
    mergeBranches.convention(
        providers.systemProperty("RELEASE_MERGE_BRANCHES").map { it == "true" }
            .orElse(providers.environmentVariable("RELEASE_MERGE_BRANCHES").map { it == "true" })
            .orElse(providers.gradleProperty("releaseMergeBranches").map { it == "true" })
            .orElse(false)
    )
}
