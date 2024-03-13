<!-- This file is auto generated during release from readme/README.md -->

[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.5.1-develop&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.5.1-develop/jar)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/de.skuzzle.restrictimports?versionSuffix=2.5.1-develop)](https://plugins.gradle.org/plugin/de.skuzzle.restrictimports/2.5.1-develop)
[![Coverage Status](https://coveralls.io/repos/github/skuzzle/restrict-imports-enforcer-rule/badge.svg?branch=master)](https://coveralls.io/github/skuzzle/restrict-imports-enforcer-rule?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/skuzzleOSS.svg?style=social)](https://twitter.com/ProjectPolly)

# restrict-imports-enforcer-rule
Keep your code base clean and free from  usage of unwanted classes! [More](#rationale)

**NEW** in 2.5.0: We now also provide a Gradle plugin!

Supported source files:
- [x] Java
- [x] Kotlin (since 0.15)
- [x] Groovy (since 0.15)

Compatibility:
- Works with Java 8+
- Tested against _maven-enforcer-plugin_ versions `1.4.1` and `3.4.1`.

## Maven quick start
This is a minimal usage example. Please scroll down for detailed configuration
information or have a look at the [Full configuration example](#full-configuration-example).

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <dependencies>
        <dependency>
            <groupId>de.skuzzle.enforcer</groupId>
            <artifactId>restrict-imports-enforcer-rule</artifactId>
            <version>2.5.1-develop</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>check-logging-imports</id> <!-- put an explanatory ID here -->
            <phase>process-sources</phase>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <RestrictImports>
                        <!-- Define an explanatory reason why these imports are prohibited -->
                        <reason>Use SLF4j for logging</reason>
                        <!-- Specify a single pattern to be banned -->
                        <bannedImport>java.util.logging.**</bannedImport>
                    </RestrictImports>

                    <!-- You could have another rule instance here for restricting further imports -->
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Gradle quick start

> [!CAUTION]
> Gradle support is quite new and should be considered experimental.
>
> Feedback is welcome and should be filed as new GitHub issue.

### ... with Groovy DSL
```
plugins {
    id("de.skuzzle.restrictimports") version("2.5.1-develop")
}

restrictImports {
    reason = "Use slf4j for logging"
    bannedImports = ["java.util.logging.**"]
}
```

### ... with Kotlin DSL
```
plugins {
    id("de.skuzzle.restrictimports") version("2.5.1-develop")
}

restrictImports {
    reason.set("Use slf4j for logging")
    bannedImports.set(listOf("java.util.logging.**"))
}
```

# Contents
* [Rationale](#rationale)
* Usage
  * [Includes and Excludes](#includes-and-excludes)
  * [Not fixable imports](#not-fixable-imports)
  * [Rule groups](#rule-groups)
  * [Static imports](#static-imports)
  * [Test code](#test-code)
  * [Skipping](#skipping)
  * [Exclude source roots](#exclude-source-roots)
  * [Parallel analysis](#parallel-analysis)
  * [Detecting full qualified class usage](#detecting-full-qualified-class-usage)
  * [Package patterns](#package-patterns)
* [Limitation](#limitation)
  * [Syntactical](#syntactical-limitation)
  * [Conceptual](#conceptual-limitation)
* [Full configuration example](#full-configuration-example)
* [Configuration options](#configuration-options)
* [Versioning, Deprecations and Compatibility](#versioning-deprecations-and-compatibility)

## Rationale
Grown code bases often have a huge number of dependencies. That leads to a lot of clutter in their
compile time classpath. My favorite example here is logging frameworks: every java project
of decent size likely has numerous classes named `Logger` available on the classpath and your
favorite IDE happily lists them all for auto completion. How should someone new to
the project know which `Logger` to use? You certainly do not want to mix logging frameworks in your
code base.

Another example is to force your developers to only use AssertJ assertions instead of JUnit or TestNG
assertions.

Using this enforcer rule gives you fine grained control over which classes are allowed to be used in your
application without having to exclude whole artifacts from your classpath.

## Includes and Excludes
To refine the classes that are banned you may use the `allowedImports` tag in addition to
the `bannedImports` tag. For example, you can exclude a whole sub package using a wildcard
operator but still allow some concrete classes:

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <bannedImport>java.util.logging.**</bannedImport>
            <allowedImport>java.util.logging.Handler</allowedImport>
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    bannedImports = listOf("java.util.logging.**")
    allowedImports = listOf("java.util.logging.Handler")
}
```
</details>

<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    bannedImports = ["java.util.logging.**"]
    allowedImports = ["java.util.logging.Handler"]
}
```
</details>


It is possible to exclude certain source files from being affected by the bans at
all. You can use `basePackage` to specify a package pattern of classes that are affected
by the rule. You may then exclude some classes to refine the matches using the
`exclusion` tag. It is also possible to specify multiple base packages.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <basePackages>
                <basePackage>com.your.domain.**</basePackage>
                <basePackage>com.your.company.**</basePackage>
            </basePackages>
            <bannedImport>java.util.logging.**</bannedImport>
            <allowedImport>java.util.logging.Handler</allowedImport>
            <!-- The following packages will not be checked for banned imports -->
            <exclusion>com.your.domain.treat.special.*</exclusion>
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    basePackes = listOf("com.your.domain.**", "com.your.company.**")
    bannedImports = listOf("java.util.logging.**")
    allowedImports = listOf("java.util.logging.Handler")
    exclusions = listOf("com.your.domain.treat.special.*")
}
```
</details>

<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    basePackes = ["com.your.domain.**", "com.your.company.**"]
    bannedImports = ["java.util.logging.**"]
    allowedImports = ["java.util.logging.Handler"]
    exclusions = ["com.your.domain.treat.special.*"]
}
```
</details>


Wherever you write package patterns you can also specify a list of patterns. Thus it is
possible to define multiple banned imports/exclusions/allowed imports or base packages.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <bannedImports>
                <bannedImport>java.util.logging.**</bannedImport>
                <bannedImport>what.ever.**</bannedImport>
            </bannedImports>
            <allowedImports>
                <allowedImport>java.util.logging.Handler</allowedImport>
                <allowedImport>what.ever.IsCool</allowedImport>
            </allowedImports>
            <exclusions>
                <exclusion>com.your.domain.treat.special.*</exclusion>
                <exclusion>com.your.domain.treat.special.too.*</exclusion>
            </exclusions>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    bannedImports = listOf("java.util.logging.**", "what.ever.**")
    allowedImports = listOf("java.util.logging.Handler", "what.ever.IsCool")
    exclusions = listOf("com.your.domain.treat.special.*", "com.your.domain.treat.special.too.*")
}
```
</details>

<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    bannedImports = ["java.util.logging.**", "what.ever.**"]
    allowedImports = ["java.util.logging.Handler", "what.ever.IsCool"]
    exclusions = ["com.your.domain.treat.special.*", "com.your.domain.treat.special.too.*"]
}
```
</details>

## Not-fixable imports
> [!NOTE]
> This is an experimental feature added in 2.4.0

In certain situations you might not be able to avoid using a banned import. For example if you implement an
interface which requires a banned type as either return- or parameter type. Instead of globally allowing such imports,
you can allow them to be used only in some explicitly configured locations.

<details open>
    <summary><b>Maven</b></summary>

You can add multiple _not-fixable_ definitions if you nest them in `<notFixables></notFixables>`.

```xml
<configuration>
    <rules>
        <RestrictImports>
            <bannedImport>com.foo.BannedClass</bannedImport>
            <notFixable>
                <in>com.yourdomain.persistence.SomeClass</in>
                <allowedImports>
                    <allowedImport>com.foo.BannedClass</allowedImport>
                </allowedImports>
                <because>Type required by implemented interface</because>
            </notFixable>
        </RestrictImports>
    </rules>
</configuration>
```
</details>

<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    bannedImports = listOf("com.foo.BannedClass")
    notFixable {
        in = "com.yourdomain.persistence.SomeClass"
        allowedImports = listOf("com.foo.BannedClass")
        because = "Type required by implemented interface"
    }
}
```
</details>

<details>
    <summary><b>Gradle (Groovy)</b></summary>

```kotlin
restrictImports {
    bannedImports = ["com.foo.BannedClass"]
    notFixable {
        in = "com.yourdomain.persistence.SomeClass"
        allowedImports = ["com.foo.BannedClass"]
        because = "Type required by implemented interface"
    }
}
```
</details>

> [!NOTE]
> Not fixable definitions can not be nested in `<groups>` (see _Rule groups_ below). Not-fixables apply globally per
> `RestrictImports` rule instance.

## Rule groups
Rule groups add another level of refining which imports will be matched. You can group
the `bannedImport(s)`, `allowedImport(s)` and `basePackage(s)` tags and specify multiple
of this groups within a single enforcer rule.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <groups>
                <group>
                    <reason>Persistence classes must only be used from within .persistence package</reason>
                    <basePackage>**</basePackage>
                    <bannedImports>
                        <bannedImport>javax.persistence.EntityManager</bannedImport>
                        <bannedImport>javax.sql.DataSource</bannedImport>
                        <bannedImport>javax.persistence.NamedQueries</bannedImport>
                        <bannedImport>javax.persistence.NamedQuery</bannedImport>
                        <bannedImport>javax.ejb.Stateful</bannedImport>
                        <bannedImport>javax.ejb.EJB</bannedImport>
                    </bannedImports>
                </group>
                <group>
                    <basePackage>com.yourdomain.persistence.**</basePackage>
                    <bannedImports>
                        <bannedImport>javax.persistence.NamedQueries</bannedImport>
                        <bannedImport>javax.persistence.NamedQuery</bannedImport>
                        <bannedImport>javax.ejb.Stateful</bannedImport>
                        <bannedImport>javax.ejb.EJB</bannedImport>
                    </bannedImports>
                </group>
            </groups>
        </RestrictImports>
    </rules>
</configuration>
```
</details>

<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    group {
        reason = "Persistence classes must only be used from within .persistence package"
        basePackages = listOf("**")
        bannedImports = listOf(
          "javax.persistence.EntityManager",
          "javax.sql.DataSource",
          "javax.persistence.NamedQueries",
          "javax.persistence.NamedQuery",
          "javax.ejb.Stateful",
          "javax.ejb.EJB"
        )
    }
    group {
        basePackages = listOf("com.yourdomain.persistence.**")
        bannedImports = listOf(
            "javax.persistence.NamedQueries",
            "javax.persistence.NamedQuery",
            "javax.ejb.Stateful",
            "javax.ejb.EJB"
        )
    }
}
```
</details>

<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    group {
        reason = "Persistence classes must only be used from within .persistence package"
        basePackages = ["**"]
        bannedImports = [
          "javax.persistence.EntityManager",
          "javax.sql.DataSource",
          "javax.persistence.NamedQueries",
          "javax.persistence.NamedQuery",
          "javax.ejb.Stateful",
          "javax.ejb.EJB"
        ]
    }
    group {
        basePackages = ["com.yourdomain.persistence.**"]
        bannedImports = [
            "javax.persistence.NamedQueries",
            "javax.persistence.NamedQuery",
            "javax.ejb.Stateful",
            "javax.ejb.EJB"
        ]
    }
}
```
</details>

When analysing a source file, the plugin collects all groups where the group's
`basePackage` matches the source file's package name. In case multiple groups are
matching, only the group with the _most specific_ base package is retained and the others
are ignored for this file. Have a look at [this](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/develop/src/test/java/de/skuzzle/enforcer/restrictimports/analyze/PackagePatternSpecifityTest.java#L34) file to have a glance at how _specificity_ works.

In the above example, the first group is chosen by default (as by `basePackage=**`) unless a class is matched by the
more specific `basePackage` of the second group. In that case, only the definitions from the second group apply to this
class.

## Static imports
> [!NOTE]
> Behavior of static import detection has been changed with version 2.0.0

Every package pattern also automatically matches `static` imports. However, it is possible to explicitly mention the
`static` keyword in the pattern. In that case, the pattern will only match a resp. static import.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <bannedImport>static org.junit.Assert.*</bannedImport>
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    bannedImports = listOf("static org.junit.Assert.*")
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    bannedImports = ["static org.junit.Assert.*"]
}
```
</details>

Inclusions and exclusion will work identically.

## Test code
By default, test code is also subject to the banned import checks (this is new since version `2.0.0`). You can disable
analysis of test code using the `includeTestCode` option.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <includeTestCode>false</includeTestCode>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    includeTestCode = false
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    includeTestCode = false
}
```
</details>

## Skipping
Using the configuration option `skip` you are able to temporarily disable a rule
instance.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <skip>true</skip>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

No direct equivalent
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

No direct equivalent
</details>

If you want banned import analysis but without breaking your build you can set
`failBuild` to `false`.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <failBuild>false</failBuild>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    failBuild = false
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    failBuild = false
}
```
</details>

You can also pass these parameters as property to the maven build using `-Drestrictimports.skip` resp.
`-Drestrictimports.failBuild`. When passed as property, the property's value takes precedence over what has been
configured in the pom file.

## Exclude source roots
By default, all source roots reported by Maven/Gradle are subject to the banned import checks, which for example includes but
is not limited to `${project.basedir}/src/main/java`, `${project.basedir}/src/test/java`,
`${project.build.directory}/generated-sources/main/java` and
`${project.build.directory}/generated-test-sources/main/java`. You can exclude source root(s) using the
`excludedSourceRoot(s)` option, either absolute or relative path.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <excludedSourceRoots>
                <excludedSourceRoot>${project.build.directory}/generated-sources/main/java</excludedSourceRoot>
                <excludedSourceRoot>target/generated-test-sources/main/java</excludedSourceRoot>
            </excludedSourceRoots>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

No direct equivalent
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

No direct equivalent
</details>

## Parallel Analysis
We support basic parallelization of the analysis. This is enabled by default but can be disabled either in the pom file
using the `<parallel>` option or by passing `-Drestrictimports.parallel` to the maven build.

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <parallel>false</parallel>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    parallel = false
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    parallel = false
}
```
</details>

## Detecting full qualified class usage
To overcome some of the limitations mentioned [here](#limitation), you can enable 'full compilation unit' parsing mode using

<details open>
    <summary><b>Maven</b></summary>

```xml
<configuration>
    <rules>
        <RestrictImports>
            <parseFullCompilationUnit>true</parseFullCompilationUnit>
            <!-- ... -->
        </RestrictImports>
    </rules>
</configuration>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
  parseFullCompilationUnit = true
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    parseFullCompilationUnit = true
}
```
</details>

The option currently only affects parsing of java source files. When enabled, we will attempt a full parse of each
java source file, creating an actual AST. This allows to also detect full qualified class usages but will be
considerably slower.

> [!WARNING]
> In case a source file cannot be properly parsed, we try to fall back to our _native_ line-by-line parsing
> approach described [here](#syntactical-limitation). A respective warning will be issued in the
> report that is generated at the end.
>
> This is especially the case when using Java language features introduced with version 16 or higher.
> See [#60](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/60) for details.

## Package Patterns

Package patterns are dot separated strings that can be compared case sensitively part by part. Every part must adhere to
the java identifier rules with the exception of a some special literals:

1. `*` matches every package part but exactly one.
2. `**` matches multiple package parts but at least one.
3. `'*'` matches a literal `*` in an import statement.

The pattern `java.util.*` matches `java.util.ArrayList` but not `java.util.regex.Pattern`.

Likewise the pattern `java.util.**` matches all classes and subclasses contained in
`java.util`. Double wildcards are supported everywhere within a pattern. `**.DumbName`
would match every import which ends in `DumbName`. Wildcards are forbidden to be used in
combination with other characters within a single part, like in `com.foo**`. Also parts
within a package must not be empty like in `foo..bar`.

If a pattern does not contain any wildcards, matching degrades to a simple String
comparison.

## Limitation

### Syntactical limitation
This rule implementation assumes that every analyzed java source file is syntactically
correct. If a source file is not, the analysis result is undefined. We don't use a formal parser to parse the whole
source file into an abstract syntax tree. Instead, import statements are extracted by relying on more or less simple
String split operations and only reading each source file up until a non-import statement (like class declaration) is
discovered. We cover a set of esoteric edge cases, for example block comments within a single import statement and the
like.

> !NOTE]
> Plus side to this approach is, that we are mostly agnostic to the Java version you are using. Our parser doesn't
> need updates even if you want to use latest Java language features in your code base.

### Conceptual limitation
Import recognition works by comparing the import statements within your source files
against the specified patterns. If your class uses wildcard imports like in

```java
import java.util.*;
```

this plugin will not be able to match that import against a banned pattern pointing to a
concrete class like `java.util.ArrayList`. However, wildcard recognition would still work
as expected.

Also, it is not possible to detect full qualified class usages, where a type is used without an import statement.

For checking the `basePackage` and `exclusion` patterns, the plugin tries to construct the
_full qualified class name_ (FQCN) of each analyzed source file. It does so by
concatenating the file name to the source file's value of the `package <value>;`
statement. Thus if your `exclusion` pattern points to a concrete class like
`com.name.ClassName` the exclusion will only match if this class is declared in a file
with the exact name `ClassName.java`. The same applies in case you use a base package
pattern with no wild cards.

## Full configuration example

<details open>
    <summary><b>Maven</b></summary>

```xml
<RestrictImports>
    <failBuild>true</failBuild> <!-- Can be overridden with -Drestrictimports.failBuild=... -->
    <skip>false</skip> <!-- Can be overridden with -Drestrictimports.skip=... -->
    <parseFullCompilationUnit>false</parseFullCompilationUnit>
    <parallel>true</parallel> <!-- Can be overridden with -Drestrictimports.parallel=... -->
    <excludedSourceRoots> <!-- Optional. Nesting not needed when specifying a excluded root -->
        <excludedSourceRoot>${project.build.directory}/generated-sources/main/java</excludedSourceRoot>
    </excludedSourceRoots>
    <groups>
        <group> <!-- Optional. groups and group can be left out in simple configurations -->
            <reason>...</reason>
            <basePackages> <!-- Optional. Nesting not needed when specifying a single package -->
                <basePackage>**</basePackage>
            </basePackages>
            <bannedImports> <!-- Optional. Nesting not needed when specifying a single package -->
                <bannedImport>..</bannedImport>
            </bannedImports>
            <allowedImports> <!-- Optional. Nesting not needed when specifying a single package -->
                <allowedImport>...</allowedImport>
            </allowedImports>
            <exclusions> <!-- Optional. Nesting not needed when specifying a single package -->
                <exclusion>...</exclusion>
            </exclusions>
        </group>
    </groups>
    <notFixables> <!-- Optional. Nesting not needed when specifying a single not-fixable -->
        <notFixable>
            <in>**</in>
            <allowedImports> <!-- Optional. Nesting not needed when specifying a single package -->
                <allowedImport>..</allowedImport>
            </allowedImports>
        </notFixable>
    </notFixables>
</RestrictImports>
```
</details>
<details>
    <summary><b>Gradle (Kotlin)</b></summary>

```kotlin
restrictImports {
    reason = "..."
    bannedImports = listOf("...")
    allowedImports = listOf("...")
    exclusions = listOf("...")
    parallel = false
    includeCompileCode = false
    includeTestCode = false
    parseFullCompilationUnit = false
}
```
</details>
<details>
    <summary><b>Gradle (Groovy)</b></summary>

```groovy
restrictImports {
    reason = "..."
    bannedImports = ["..."]
    allowedImports = ["..."]
    exclusions = ["..."]
    parallel = false
    includeCompileCode = false
    includeTestCode = false
    parseFullCompilationUnit = false
}
```
</details>
