plugins {
    id("build-logic.release-lifecycle")
}

tasks {
    val quickCheck by registering {
        group = "Lifecycle"
    }
}
