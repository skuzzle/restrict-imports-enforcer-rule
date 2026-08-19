import de.skuzzle.buildlogic.accepttos.AcceptGradleToSTask

tasks.register<AcceptGradleToSTask>("acceptToS") {
    markerFile = file(AcceptGradleToSTask.ACCEPT_FILE_NAME).absolutePath
}
