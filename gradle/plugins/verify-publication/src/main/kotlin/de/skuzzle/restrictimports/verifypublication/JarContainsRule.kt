package de.skuzzle.restrictimports.verifypublication

import org.gradle.api.Action

class JarContainsRule() {
    val fileRules: MutableList<FileMatchesRule> = mutableListOf()

    fun aFile(pathInJar: String, action: Action<FileMatchesRule>): JarContainsRule {
        val rule = FileMatchesRule(pathInJar)
        action.execute(rule)
        fileRules.add(rule)
        return this
    }

    fun aFile(pathInJar: String): JarContainsRule {
        return aFile(pathInJar) { }
    }
}
