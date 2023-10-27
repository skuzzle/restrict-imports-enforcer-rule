import org.gradle.api.DefaultTask

val prepareRelease by tasks.creating(DefaultTask::class.java) {
    description = "Hook task you can depend on to run something before release"
    group = "release"
}
