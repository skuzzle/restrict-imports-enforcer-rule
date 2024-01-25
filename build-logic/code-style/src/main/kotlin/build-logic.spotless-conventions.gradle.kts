plugins {
    id("com.diffplug.spotless")
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.gradle", "gradle/**/*.gradle", "gradle/**/*.gradle.kts", "*.gitignore")
        targetExclude("gradle/**/build/**")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    format("documentation") {
        target("*.adoc", "*.md", "src/**/*.adoc", "src/**/*.md")
        trimTrailingWhitespace()
        endWithNewline()
    }

    groovy {
        target("**Jenkinsfile", "**JenkinsfileRelease")
        greclipse()
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    pluginManager.withPlugin("java") {
        val configDir = rootProject.layout.projectDirectory.dir("code-style/eclipse")
        val importOrderConfigFile = configDir.file("eclipse.importorder")
        val javaFormatterConfigFile = configDir.file("formatter-settings.xml")

        java {
            toggleOffOn()
            importOrderFile(importOrderConfigFile)
            eclipse().configFile(javaFormatterConfigFile)
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
