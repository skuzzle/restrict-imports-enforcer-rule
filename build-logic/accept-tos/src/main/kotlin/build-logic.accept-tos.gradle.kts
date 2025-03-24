import de.skuzzle.buildlogic.accepttos.AcceptGradleToSTask

val acceptToS by tasks.registering(AcceptGradleToSTask::class) {
    markerFile.set(file(AcceptGradleToSTask.ACCEPT_FILE_NAME).absolutePath)
}
