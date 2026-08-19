plugins {
    id("build-logic.base")
    id("java-library")
    id("jacoco")
    id("jvm-test-suite")
}

// Version production code is compiled *for*, see the --release usage below
val productionCodeJavaVersion = JavaLanguageVersion.of(8)

// Version used to compile and run everything that is not published
val buildJavaVersion = JavaLanguageVersion.of(21)

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {

    compileJava {
        // Production code is compiled by a current compiler targeting productionCodeJavaVersion
        // via --release, rather than by a compiler of that version. As of Gradle 9 the gradleApi()
        // dependency consists of Java 17 class files, which a Java 8 compiler can not read at all.
        // --release still emits bytecode for, and limits the code to the API of, that version.
        javaCompiler = javaToolchains.compilerFor {
            languageVersion = buildJavaVersion
        }
        options.release = productionCodeJavaVersion.asInt()
    }

    compileTestJava {
        javaCompiler = javaToolchains.compilerFor {
            languageVersion = buildJavaVersion
        }
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

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter(requiredVersionFromLibs("junit5"))
    targets {
        all {
            testTask {
                javaLauncher.set(javaToolchains.launcherFor {
                    languageVersion = buildJavaVersion
                })
            }
        }
    }
}

repositories {
    mavenCentral()
}
