plugins {
    id("build-logic.release-lifecycle")
}

tasks {
    val quickCheck by creating {
        group = "Lifecycle"
    }
}
