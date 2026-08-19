import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradle.commonCustomUserData)
    implementation(libs.gradle.develocity)
    implementation(libs.shadowPlugin)
    implementation(libs.foojayResolver)
    implementation(projects.releasePlugin)
    implementation(projects.verifyPublication)
    implementation(projects.codeStyle)
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
