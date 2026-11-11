import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

fun Project.requiredVersionFromLibs(name: String) =
    libsVersionCatalog.findVersion(name).get().requiredVersion

private val Project.libsVersionCatalog: VersionCatalog
    get() = the<VersionCatalogsExtension>().named("libs")

fun Project.allJavaModules() =
    project.rootProject.subprojects.filter { it.pluginManager.hasPlugin("java-library") }
