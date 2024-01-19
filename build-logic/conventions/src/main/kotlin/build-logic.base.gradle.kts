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

