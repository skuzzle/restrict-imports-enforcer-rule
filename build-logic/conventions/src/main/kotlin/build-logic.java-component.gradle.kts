import com.diffplug.gradle.spotless.SpotlessCheck

plugins {
    id("build-logic.base")
    id("java-library")
    id("jacoco")
}

val productionCodeJavaVersion = JavaLanguageVersion.of(8)
val testCodeJavaVersion = JavaLanguageVersion.of(11)

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {

    compileJava {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion = productionCodeJavaVersion
        }
    }

    compileTestJava {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion = testCodeJavaVersion
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion = testCodeJavaVersion
        })
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs + "-parameters"
    }

    javadoc {
        options {
            (this as StandardJavadocDocletOptions).apply {
                tags = listOf(
                    "apiNote:a:API Note:",
                    "implSpec:a:Implementation Requirements:",
                    "implNote:a:Implementation Note:"
                )
            }
        }
    }
}

repositories {
    mavenCentral()
}
