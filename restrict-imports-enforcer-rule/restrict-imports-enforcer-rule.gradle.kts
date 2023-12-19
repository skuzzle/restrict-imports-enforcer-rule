plugins {
    id("published-java-component")
    id("verify-publication-conventions")
}

description = "Restrict Imports Enforcer Rule"
extra.apply {
    set("automaticModuleName", "de.skuzzle.enforcer.restrictimports.rule")
}
dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
    implementation(libs.slf4j)

    implementation(libs.maven.core)
    implementation(libs.maven.plugin.api)
    implementation(libs.maven.enforcer.api)

    testImplementation(libs.mockito)
    testImplementation(libs.bytebuddy)
    testImplementation(libs.bytebuddyAgent)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.equalsverifier)
    testImplementation(libs.assertj.core)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.jimfs)
}

val maven by publishing.publications.creating(MavenPublication::class) {
    artifactId = "restrict-imports-enforcer-rule"
    artifact(tasks.named("javadocJar"))
    artifact(tasks.named("sourcesJar"))
    shadow.component(this)
}

verifyPublication {
    expectPublishedArtifact("restrict-imports-enforcer-rule") {
        withClassifiers("", "javadoc", "sources")
//        dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()
    }
}
