package de.skuzzle.mavenwrapper

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class MavenDownloadExtension(val project: Project) {

    companion object {
        val NAME = "maven"
    }

    abstract val baseDir: DirectoryProperty

    abstract val tasks: MapProperty<String, TaskProvider<out Task>>

    fun mavenHome(version: String): File {
        return baseDir.dir("$version/apache-maven-$version").get().asFile
    }

    fun download(version: String): TaskProvider<out Task> {
        if (tasks.get().contains(version)) {
            return tasks.get()[version]!!
        }
        val maven = project.configurations.create("mavenDistribution_$version")

        project.dependencies {
            maven("org.apache.maven:apache-maven:$version") {
                artifact {
                    type = "zip"
                    extension = "zip"
                    classifier="bin"
                }
                isTransitive = false
            }
        }

        val downloadTask = project.tasks.register<Sync>("downloadMaven${version}") {
            from(project.zipTree( maven.singleFile))
            into(baseDir.dir(version))
        }

        tasks.put(version, downloadTask)
        return downloadTask
    }
}
