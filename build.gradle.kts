
plugins {
    alias(libs.plugins.nexus.publish)
    id("build-logic.base")
    id("build-logic.release")
    id("build-logic.accept-tos")
}

release {
    mainBranch.set("master")
    devBranch.set("develop")
    githubRepoOwner.set("skuzzle")
    githubRepoName.set("restrict-imports-enforcer-rule")
    releaseNotesContent.set(providers.fileContents(layout.projectDirectory.file("RELEASE_NOTES.md")).asText)
}

nexusPublishing.repositories {
    sonatype {
        username.set(property("sonatype_USR").toString())
        password.set(property("sonatype_PSW").toString())
    }
}
