[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=3.0.2&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/3.0.2/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=3.0.2)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/3.0.2)

> [!NOTE]
> This release contains no functional changes. A release dry run accidentally promoted its
> staging repository, which published `3.0.2-develop` to Maven Central and left it there as
> the latest version of `de.skuzzle.enforcer:restrict-imports-enforcer-rule`. This release
> supersedes those artifacts with a proper release. Do not use `3.0.2-develop`.

### Documentation
* [#278](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/278) The legacy `buildscript` snippet below now declares the Gradle plugin under `de.skuzzle.restrictimports:restrict-imports-gradle-plugin`. The `de.skuzzle.enforcer` coordinates documented until 3.0.1 never existed on Maven Central, so the declared dependency could not be resolved

### Dependency coordinates
<details>
    <summary><b>Maven dependency declaration</b></summary>

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>3.0.2</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrictimports") version "3.0.2"
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
    classpath("de.skuzzle.restrictimports:restrict-imports-gradle-plugin:3.0.2")
  }
}

apply(plugin = "de.skuzzle.restrictimports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrictimports", version = "3.0.2" }
```
</details>
