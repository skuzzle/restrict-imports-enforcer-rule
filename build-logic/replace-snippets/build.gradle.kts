import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `groovy`
}

repositories {
    mavenLocal()
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation(kotlin("gradle-plugin"))
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
            }
        }
    }
}
