[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=@project.version@&color=blue)](https://search.maven.org/artifact/@project.groupId@/restrict-imports-enforcer-rule/@project.version@/jar)

> [!NOTE]
> This is the first release after migrating our build to Gradle and which uses shaded dependencies.
> If you encounter any irregularities with this version, please do not hesitate to file an issue.

### Features
* [#38](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/38) Dependencies are shaded into plugin artifacts
* [#59](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/59) Provide a Gradle plugin

Maven Central coordinates for this release:

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
  id("de.skuzzle.restrict.imports") version "@project.version@"
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

apply(plugin = "de.skuzzle.restrict.imports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrict-imports = { id = "de.skuzzle.restrict.imports", version = "@project.version@" }
```
