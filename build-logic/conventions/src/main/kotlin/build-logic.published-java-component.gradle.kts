import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // NOTE: plugin application order is relevant here!
    id("build-logic.publishing-conventions")
    id("build-logic.java-component")
    id("build-logic.verify-publication")
    id("com.github.johnrengelman.shadow")
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier = ""
}
