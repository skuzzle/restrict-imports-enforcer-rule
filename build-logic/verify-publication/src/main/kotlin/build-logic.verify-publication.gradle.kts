import de.skuzzle.restrictimports.verifypublication.VerifyPublicationExtension
import de.skuzzle.restrictimports.verifypublication.VerifyPublicationTask

plugins {
    `maven-publish`
}

val extension = extensions.create<VerifyPublicationExtension>(VerifyPublicationExtension.NAME).apply {
    groupId = project.group.toString()
}

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

val clearTempRepo by tasks.registering(Delete::class) {
    delete(extension.verificationRepoDir)
}

val verifyPublication by tasks.registering(VerifyPublicationTask::class) {
    group = "verification"
    description = "Verifies structure and contents of all published artifacts"
    dependsOn(clearTempRepo, publishToVerificationRepoTasks)

    version = project.version.toString()
    groupId = extension.groupId
    verificationRepoDir = extension.verificationRepoDir
    artifacts = extension.artifacts
}
tasks.named("check").configure { dependsOn(verifyPublication) }
