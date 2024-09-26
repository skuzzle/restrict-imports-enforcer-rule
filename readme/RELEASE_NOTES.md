[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=@project.version@&color=blue)](https://search.maven.org/artifact/@project.groupId@/restrict-imports-enforcer-rule/@project.version@/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/@project.pluginId@?versionSuffix=@project.version@)](https://plugins.gradle.org/plugin/@project.pluginId@/@project.version@)

### Features
* [#177](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/177) Support matching prefix, suffix and infix
parts within a package pattern.

### Dependency coordinates
<details>
    <summary><b>Maven dependency declaration</b></summary>

```xml
<dependency>
    <groupId>@project.groupId@</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>@project.version@</version>
</dependency>
```
</details>

<details>
    <summary><b>Gradle dependency declaration</b></summary>

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
</details>
