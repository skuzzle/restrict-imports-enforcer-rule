import de.skuzzle.restrictimports.verifypublication.VerifyPublicationExtension
import de.skuzzle.restrictimports.verifypublication.VerifyPublicationTask

plugins {
    `maven-publish`
}

val extension = extensions.create<VerifyPublicationExtension>(VerifyPublicationExtension.NAME)
extension.verificationRepoDir.convention(layout.buildDirectory.dir("verifyPublication/repository"))

publishing {
    repositories {
        maven {
            name = "verifyPublication"
            url = extension.verificationRepoDir.get().asFile.toURI()
        }
    }
}

val publishToVerificationRepoTasks = tasks.withType<PublishToMavenRepository>().matching { it.name.endsWith("VerifyPublicationRepository") }
publishToVerificationRepoTasks.configureEach {
    outputs.dir(extension.verificationRepoDir.locationOnly)
    mustRunAfter(clearTempRepo)
}

val clearTempRepo by tasks.creating(Delete::class.java) {
    delete(extension.verificationRepoDir)
}

val verifyPublication by tasks.creating(VerifyPublicationTask::class.java) {
    group = "verification"
    description = "Verifies structure and contents of all published artifacts"
    dependsOn(clearTempRepo, publishToVerificationRepoTasks)

    version = project.version.toString()
    groupId = project.group.toString()
    verificationRepoDir = extension.verificationRepoDir
    artifacts = extension.artifacts
}
