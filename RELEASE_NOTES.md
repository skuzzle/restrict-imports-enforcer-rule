[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.5.0&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.5.0/jar)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=2.5.0)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/2.5.0)

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
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>2.5.0</version>
</dependency>
```

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrict.imports") version "2.5.0"
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
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:2.5.0")
  }
}

apply(plugin = "de.skuzzle.restrict.imports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrict.imports", version = "2.5.0" }
```
