plugins {
    id("build-logic.java-component")
}

description = "Restrict Imports Enforcer Rule Core"

dependencies {
    implementation(libs.slf4j)
    implementation(libs.javaparser)

    testImplementation(libs.mockito)
    testImplementation(libs.bytebuddy)
    testImplementation(libs.bytebuddyAgent)

    testImplementation(libs.equalsverifier)
    testImplementation(libs.assertj.core)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.jimfs)
}
