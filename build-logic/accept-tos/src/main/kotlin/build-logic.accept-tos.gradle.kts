import de.skuzzle.buildlogic.accepttos.AcceptGradleToSTask

val acceptToS by tasks.registering(AcceptGradleToSTask::class) {
    markerFile = file(AcceptGradleToSTask.ACCEPT_FILE_NAME).absolutePath
}
