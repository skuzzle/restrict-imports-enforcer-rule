import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation(libs.gradle.commonCustomUserData)
    implementation(libs.gradle.enterprise)
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
