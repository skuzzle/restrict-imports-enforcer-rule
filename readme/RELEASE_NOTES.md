[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=@project.version@&color=blue)](https://search.maven.org/artifact/@project.groupId@/restrict-imports-enforcer-rule/@project.version@/jar)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/@project.pluginId@?versionSuffix=@project.version@)](https://plugins.gradle.org/plugin/@project.pluginId@/@project.version@)

> [!NOTE]
> This is the first release after migrating our build to Gradle and which uses shaded dependencies.
> If you encounter any irregularities with this version, please do not hesitate to file an issue.

### Features
* [#38](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/38) Dependencies are shaded into plugin artifacts
* [#59](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/59) Provide a Gradle plugin
* [#118](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/118) Print absolute paths in exception messages to make IntelliJ render clickable links

maven
```xml
<dependency>
    <groupId>@project.groupId@</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>@project.version@</version>
</dependency>
```

Gradle plugin DSL
```groovy
plugins {
  id("@project.pluginId@") version "@project.version@"
}
```

Gradle Legacy
```groovy
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:@project.version@")
  }
}

apply(plugin = "@project.pluginId@")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "@project.pluginId@", version = "@project.version@" }
```
