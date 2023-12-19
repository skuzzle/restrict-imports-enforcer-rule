[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.4.1-refactor-release-SNAPSHOT&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.4.1-refactor-release-SNAPSHOT/jar)

### Bug fixes
* [#59](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/59) Provide a Gradle plugin


Maven Central coordinates for this release:

maven
```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>2.4.1-refactor-release-SNAPSHOT</version>
</dependency>
```

Gradle plugin DSL
```groovy
plugins {
  id("de.skuzzle.restrict.imports") version "2.4.1-SNAPSHOT"
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
    classpath("de.skuzzle.enforcer:restrict-imports-gradle-plugin:2.4.1-SNAPSHOT")
  }
}

apply(plugin = "de.skuzzle.restrict.imports")
```

Gradle version catalog (Toml)
```toml
[plugins]
restrict-imports = { id = "de.skuzzle.restrict.imports", version = "2.4.1-SNAPSHOT" }
```
