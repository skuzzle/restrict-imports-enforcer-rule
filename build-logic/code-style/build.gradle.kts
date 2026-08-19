import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradle.spotless)
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
