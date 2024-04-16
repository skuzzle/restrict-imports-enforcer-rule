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
        options.release.set(11)
    }
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
            allWarningsAsErrors = true
        }
    }
}
