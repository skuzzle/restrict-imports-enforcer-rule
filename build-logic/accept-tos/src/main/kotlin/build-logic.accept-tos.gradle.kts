import de.skuzzle.buildlogic.accepttos.AcceptGradleToSTask

val acceptToS by tasks.creating(AcceptGradleToSTask::class.java) {
    markerFile.set(file(AcceptGradleToSTask.ACCEPT_FILE_NAME).absolutePath)
}
