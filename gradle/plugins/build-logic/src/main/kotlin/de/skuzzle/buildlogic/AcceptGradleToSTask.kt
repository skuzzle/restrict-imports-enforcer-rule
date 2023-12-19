package de.skuzzle.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.Path

abstract class AcceptGradleToSTask : DefaultTask() {

    @get:Input
    abstract val markerFile: Property<String>

    @TaskAction
    fun action() {
        val markerFilePath = Path.of(markerFile.get())
        if (Files.exists(markerFilePath)) {
            println("You already accepted the ToS for publishing build scans. If you want to revoke that decision, simply delete the marker file '${markerFilePath.fileName}'")
            return
        }

        Files.writeString(
            markerFilePath,
            """
            You have accepted the Terms Of Service for publishing Build Scans to https://scans.gradle.com

            You can read the ToS here: https://gradle.com/terms-of-service

            You can opt-out of publishing build scans every time by simply deleting this marker file.

            PLEASE DON'T COMMIT THIS FILE TO THE REPOSITORY!
            """.trimIndent()
        )

        println("You have successfully opted into publishing build scans by accepting the Gradle Terms of Service. If you want to revoke that decision, simply delete the marker file '${markerFilePath.fileName}'")
    }
}
