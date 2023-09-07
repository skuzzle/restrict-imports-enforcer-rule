plugins {
    `published-java-component`
}
description = "Restrict Imports Enforcer Rule"
extra.apply {
    set("automaticModuleName", "de.skuzzle.enforcer.restrictimports.rule")
}
dependencies {
    implementation(projects.restrictImportsEnforcerRuleCore)
    implementation(libs.slf4j)

    implementation(libs.maven.core)
    implementation(libs.maven.plugin.api)
    implementation(libs.maven.enforcer.api)

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
