import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // NOTE: plugin application order is relevant here!
    id("publishing-conventions")
    id("java-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")
}
