package de.skuzzle.restrictimports.verifypublication

class FileMatchesRule(val pathInJar: String) {
    val contentMatcher: MutableList<MatcherWithDescription> = mutableListOf()

    fun matching(message: String, contentMatcher: (content: String) -> Boolean): FileMatchesRule {
        this.contentMatcher.add(MatcherWithDescription(contentMatcher, message))
        return this
    }
}
