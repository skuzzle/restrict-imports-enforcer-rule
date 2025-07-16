import de.skuzzle.mavenwrapper.MavenDownloadExtension

 extensions.create<MavenDownloadExtension>(MavenDownloadExtension.NAME).apply {
    baseDir.convention(layout.buildDirectory.dir("maven-dist"))
}
