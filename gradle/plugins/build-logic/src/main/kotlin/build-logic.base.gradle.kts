import de.skuzzle.restrictimports.verifypublication.VerifyPublicationTask

plugins {
    base
    eclipse
    idea
    id("build-logic.spotless-conventions")
    id("build-logic.lifecycle")
}

repositories {
    mavenCentral()
}

tasks.named("quickCheck").configure { dependsOn(tasks.withType<VerifyPublicationTask>()) }
