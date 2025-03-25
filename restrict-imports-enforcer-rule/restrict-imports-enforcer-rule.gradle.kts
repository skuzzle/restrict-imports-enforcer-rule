plugins {
    id("build-logic.published-java-component")
    id("build-logic.release-lifecycle")
}

description = "Restrict Imports Enforcer Rule"

dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
    implementation(libs.slf4j)

    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.enforcer.api)

    testImplementation(libs.mockito)
    testImplementation(libs.bytebuddy)
    testImplementation(libs.bytebuddyAgent)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

    testImplementation(libs.equalsverifier)
    testImplementation(libs.assertj.core)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.jimfs)

    testImplementation(libs.maven.core)
    testImplementation(libs.maven.plugin.api)
    testImplementation(libs.maven.enforcer.api)
}

val maven by publishing.publications.creating(MavenPublication::class) {
    artifactId = "restrict-imports-enforcer-rule"
    artifact(tasks.named("javadocJar"))
    artifact(tasks.named("sourcesJar"))
    shadow.component(this)
}

tasks.jar.configure {
    onlyIf { false }
}

verifyPublication {
    expectPublishedArtifact("restrict-imports-enforcer-rule") {
        withClassifiers("", "javadoc", "sources")
//        dependencies should be shadowed
        withPomFileContentMatching("Should have no <dependencies>") { content -> !content.contains("<dependencies>") }
        withPomFileMatchingMavenCentralRequirements()
        withJarContaining {
            // Test for shadowed files
            aFile("de/skuzzle/enforcer/restrictimports/analyze/AnalyzeResult.class")
            aFile("META-INF/services/de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport") {
                matching("") {content ->
                    content.contains("de.skuzzle.enforcer.restrictimports.parser.lang.KotlinGroovyLanguageSupport")
                }
            }
        }
    }
}

tasks.prepareRelease.configure { dependsOn(tasks.publishToSonatype) }
