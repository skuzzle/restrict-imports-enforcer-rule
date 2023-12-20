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

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = productionCodeJavaVersion
    }
}

tasks.compileTestJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = testCodeJavaVersion
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion = testCodeJavaVersion
    })
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
                "implNote:a:Implementation Note:"
            )
        }
    }
}

repositories {
    mavenCentral()
}

