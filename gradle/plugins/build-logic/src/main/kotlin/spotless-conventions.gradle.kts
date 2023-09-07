plugins {
    id("com.diffplug.spotless")
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.gradle", "buildSrc/**/*.gradle", "buildSrc/**/*.gradle.kts", "*.gitignore")
        targetExclude("buildSrc/build/**")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }

    format("documentation") {
        target("*.adoc", "*.md", "src/**/*.adoc", "src/**/*.md")
        trimTrailingWhitespace()
        endWithNewline()
    }

    pluginManager.withPlugin("java") {
        val configDir = rootProject.layout.projectDirectory.dir("gradle/config/eclipse")
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
