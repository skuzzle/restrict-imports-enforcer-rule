[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.6.1&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.6.1/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=2.6.1)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/2.6.1)

### Bug fixes
* [#221](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/221) Use of parseFullCompilationUnit=true breaks wildcard literal matches
* [#220](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/220) Add error message when multiple groups have identical basePackages

### Dependency coordinates
<details>
    <summary><b>Maven dependency declaration</b></summary>

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>2.6.1</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrictimports") version "2.6.1"
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
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:2.6.1")
  }
}

apply(plugin = "de.skuzzle.restrictimports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrictImports = { id = "de.skuzzle.restrictimports", version = "2.6.1" }
```
</details>
