[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=3.0.0&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/3.0.0/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=3.0.0)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/3.0.0)

> [!NOTE]
> This version removes support for all enforcer-plugin versions prior to 3.2.1

### Features
* [#90](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/90) Replace implementation of deprecated Maven interfaces `EnforcerRule` and `EnforcerRule2` with using `AbstractEnforcerRule`

### Dependency coordinates
<details>
    <summary><b>Maven dependency declaration</b></summary>

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>3.0.0</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrictimports") version "3.0.0"
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
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:3.0.0")
  }
}

apply(plugin = "de.skuzzle.restrictimports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrictimports", version = "3.0.0" }
```
</details>
