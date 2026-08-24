[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=3.0.1&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/3.0.1/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=3.0.1)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/3.0.1)

> [!NOTE]
> The Gradle plugin is now tested against the latest Gradle 7.x, 8.x and 9.x releases

### Bug fixes
* [#274](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/274) The Gradle plugin no longer uses the deprecated `Project.getProperties` method, which emits a deprecation warning since Gradle 9.6 and would have become an error in Gradle 10

### Dependency coordinates
<details>
    <summary><b>Maven dependency declaration</b></summary>

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>3.0.1</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrictimports") version "3.0.1"
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
    classpath("de.skuzzle.restrictimports:restrict-imports-gradle-plugin:3.0.1")
  }
}

apply(plugin = "de.skuzzle.restrictimports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrictimports", version = "3.0.1" }
```
</details>
