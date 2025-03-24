plugins {
    base
}

val test by tasks.registering {
    group = "verification"
}

val quickCheck by tasks.registering {
    group = "verification"
}

fun TaskContainer.connectSubprojectTasks(taskName: String) {
    named(taskName) {
        val tasks = project.getTasksByName(name, true)
        tasks.remove(this)
        dependsOn(tasks)
    }
}

tasks {
    listOf("clean", "check", "test", "quickCheck").forEach {
        connectSubprojectTasks(it)
    }
}
