[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=@project.version@&color=blue)](https://search.maven.org/artifact/@project.groupId@/restrict-imports-enforcer-rule/@project.version@/jar) [![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/@project.pluginId@?versionSuffix=@project.version@)](https://plugins.gradle.org/plugin/@project.pluginId@/@project.version@)

> [!NOTE]
> The Gradle plugin is now tested against the latest Gradle 7.x, 8.x and 9.x releases

### Bug fixes
* [#274](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/274) The Gradle plugin no longer uses the deprecated `Project.getProperties` method, which emits a deprecation warning since Gradle 9.6 and would have become an error in Gradle 10

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
    classpath("de.skuzzle.restrictimports:restrict-imports-gradle-plugin:@project.version@")
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
