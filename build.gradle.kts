import de.skuzzle.buildlogic.AcceptGradleToSTask

plugins {
    alias(libs.plugins.nexus.publish)
    `base-conventions`
    id("release-conventions")
}

release {
    mainBranch.set("master")
    devBranch.set("develop")
    releaseNotesContent.set(providers.fileContents(layout.projectDirectory.file("RELEASE_NOTES.md")).asText)
}

nexusPublishing.repositories {
    sonatype {
        username.set(property("sonatype_USR").toString())
        password.set(property("sonatype_PSW").toString())
    }
}

val acceptToS by tasks.creating(AcceptGradleToSTask::class.java) {
    markerFile.set(file("YOU ACCEPTED THE TOS FOR PUBLISHING BUILD SCANS").absolutePath)
}
