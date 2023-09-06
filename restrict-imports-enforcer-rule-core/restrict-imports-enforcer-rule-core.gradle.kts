plugins {
    `published-java-component`
}
description = "Restrict Imports Enforcer Rule Core"
extra.apply {
    set("automaticModuleName", "de.skuzzle.enforcer.restrictimports.core")
}
dependencies {
    implementation(libs.slf4j)
    implementation(libs.javaparser)

    testImplementation(libs.mockito)
    testImplementation(libs.bytebuddy)
    testImplementation(libs.bytebuddyAgent)

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.equalsverifier)
    testImplementation(libs.assertj.core)
    testImplementation(libs.equalsverifier)
    testImplementation(libs.jimfs)
}
