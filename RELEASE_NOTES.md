[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.6.2-skuzzle-dependency-updates&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.6.2-skuzzle-dependency-updates/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=2.6.2-skuzzle-dependency-updates)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/2.6.2-skuzzle-dependency-updates)

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
    <version>2.6.2-skuzzle-dependency-updates</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrictimports") version "2.6.2-skuzzle-dependency-updates"
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
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:2.6.2-skuzzle-dependency-updates")
  }
}

apply(plugin = "de.skuzzle.restrictimports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrictimports", version = "2.6.2-skuzzle-dependency-updates" }
```
</details>
