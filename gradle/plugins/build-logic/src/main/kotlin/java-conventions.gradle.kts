import com.gradle.enterprise.gradleplugin.testselection.PredictiveTestSelectionProfile;

plugins {
    id("base-conventions")
    id("java-library")
    id("jacoco")
}

val productionCodeJavaVersion = JavaLanguageVersion.of(8)
val testCodeJavaVersion = JavaLanguageVersion.of(11)

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs + "-parameters"
}

tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            tags = listOf(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:")
        }
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(testCodeJavaVersion)
    })
}

tasks.compileJava {
    sourceCompatibility = productionCodeJavaVersion.toString()
    targetCompatibility = productionCodeJavaVersion.toString()
}

tasks.compileTestJava {
    sourceCompatibility = testCodeJavaVersion.toString()
    targetCompatibility = testCodeJavaVersion.toString()
}
