plugins {
    base
}

tasks.register("test") {
    group = "verification"
}

tasks.register("quickCheck") {
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
