import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `groovy`
}


dependencies {
    implementation(kotlin("gradle-plugin"))
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 17
    }
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            allWarningsAsErrors = true
        }
    }
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(project())
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
            }
        }
    }
}
gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
tasks.check.configure { dependsOn("functionalTest") }
