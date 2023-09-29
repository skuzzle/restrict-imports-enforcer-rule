plugins {
    alias(libs.plugins.gradlePluginPublish)
    `kotlin-dsl`
    groovy
    `jvm-test-suite`
    `published-java-component`
}

description = "Restrict Imports Gradle Plugin"
extra.apply {
    set("automaticModuleName", "de.skuzzle.restrictimports.gradle")
}

gradlePlugin {
    val restrictImports by plugins.creating {
        id = "de.skuzzle.restrict.imports"
        implementationClass = "de.skuzzle.restrictimports.gradle.RestrictImportsPlugin"
        displayName = "Restrict Imports Gradle Plugin"
        description = project.description
    }
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useSpock(libs.versions.spock)

            dependencies {
                implementation(project())
                implementation(platform(libs.groovy.bom.get().toString()))
                implementation(libs.groovy.nio)
            }
        }

        tasks.named<Task>("check") {
            dependsOn(functionalTest)
        }
    }
}
gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])
