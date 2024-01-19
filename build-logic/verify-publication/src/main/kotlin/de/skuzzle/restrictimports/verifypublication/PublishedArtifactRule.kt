package de.skuzzle.restrictimports.verifypublication

import org.gradle.api.Action

class PublishedArtifactRule(val name: String) {

    val filesInJar: MutableList<JarContainsRule> = mutableListOf()
    val classifiers: MutableSet<String> = mutableSetOf()
    val pomFileMatchers: MutableList<MatcherWithDescription> = mutableListOf()

    fun withClassifiers(vararg classifiers: String): PublishedArtifactRule {
        this.classifiers.addAll(classifiers)
        return this
    }

    fun withPomFileContentMatching(message: String, pomFileMatcher: (String) -> Boolean): PublishedArtifactRule {
        pomFileMatchers.add(MatcherWithDescription(pomFileMatcher, message))
        return this
    }

    fun withPomFileMatchingMavenCentralRequirements(): PublishedArtifactRule {
        withPomFileContentMatching("Missing <name>") { it.contains("<name>") }
        withPomFileContentMatching("Missing <url>") { it.contains("<url>") }
        withPomFileContentMatching("Missing <description>") { it.contains("<description>") }
        withPomFileContentMatching("Missing <license>") { it.contains("<license>") }
        withPomFileContentMatching("Missing <scm>") { it.contains("<scm>") }
        return this
    }

    fun withJarContaining(jar: Action<JarContainsRule>): PublishedArtifactRule {
        val f = JarContainsRule()
        jar.execute(f)
        filesInJar.add(f)
        return this
    }
}
