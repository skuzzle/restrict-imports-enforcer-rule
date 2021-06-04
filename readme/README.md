[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule)
[![Build Status](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule.svg?branch=master)](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule) 
[![Coverage Status](https://coveralls.io/repos/skuzzle/restrict-imports-enforcer-rule/badge.svg?branch=master&service=github)](https://coveralls.io/github/skuzzle/restrict-imports-enforcer-rule?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/skuzzleOSS.svg?style=social)](https://twitter.com/skuzzleOSS)

# restrict-imports-enforcer-rule
Maven enforcer rule that bans certain imports. Keep your code base clean and free from 
usage of unwanted classes! [More](#rationale)
- [x] Java
- [x] Kotlin (since 0.15)
- [x] Groovy (since 0.15)
- [ ] Scala (see [Issue 24](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/24))

## Simple usage
This is a minimal usage example. Please scroll down for detailed configuration 
information.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>${version.enforcer-api}</version>
    <dependencies>
        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>${project.artifactId}</artifactId>
            <version>${project.version}</version>
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
                    <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
                        <!-- Define an explanatory reason why these imports are prohibited -->
                        <reason>Use SLF4j for logging</reason>
                        <!-- Specify a single pattern to be banned -->
                        <bannedImport>java.util.logging.**</bannedImport>
                    </restrictImports>
        
                    <!-- You could have another rule instance here for restricting further imports -->
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

# Contents
* [Rationale](#rationale)
* Usage
  * [Includes and Excludes](#includes-and-excludes)
  * [Rule groups](#rule-groups)
  * [Static imports](#static-imports)
  * [Test code](#test-code)
  * [Skipping](#skipping)
  * [Package patterns](#package-patterns)
* [Limitation](#limitation)
  * [Syntactical](#syntactical-limitation)
  * [Conceptual](#conceptual-limitation)
* [Configuration options](#configuration-options)
* [Versioning and Compatibility](#versioning-and-compatibility)
* [Changelog](#changelog)

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
the `bannedImports` tag. For example you can exclude a whole sub package using a wildcard
operator and then include some concrete classes:

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <bannedImport>java.util.logging.**</bannedImport>
            <allowedImport>java.util.logging.Handler</allowedImport>
        </restrictImports>
    </rules>
</configuration>
```

It is possible to exclude certain source files from being affected by the bans at 
all. You can use `basePackage` to specify a package pattern of classes that are affected 
by the rule. You may then exclude some classes to refine the matches using the
`exclusion` tag. It is also possible to specify multiple base packages.

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <basePackages>
                <basePackage>com.your.domain.**</basePackage>
                <basePackage>com.your.company.**</basePackage>
            </basePackages>
            <bannedImport>java.util.logging.**</bannedImport>
            <allowedImport>java.util.logging.Handler</allowedImport>
            <!-- The following packages will not be checked for banned imports -->
            <exclusion>com.your.domain.treat.special.*</exclusion>
        </restrictImports>
    </rules>
</configuration>
```

Wherever you write package patterns you can also specify a list of patterns. Thus it is 
possible to define multiple banned imports/exclusions/allowed imports or base packages.

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
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
        </restrictImports>
    </rules>
</configuration>
```

## Rule groups
(*Note:* This is a beta feature and not thoroughly tested. Syntax and behavior 
changes in upcoming versions are likely)

Rule groups add another level of refining which imports will be matched. You can group
the `bannedImport(s)`, `allowedImport(s)` and `basePackage(s)` tags and specify multiple 
of this groups within a single enforcer rule. 

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
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
        </restrictImports>
    </rules>
</configuration>
```

When analysing a source file, the plugin filters all groups where the group's 
`basePackage` matches the source file's package name. In case multiple groups are 
matching, only the group with the _most specific_ base package is retained and the others 
are ignored for this file. Have a look at [this](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/develop/src/test/java/de/skuzzle/enforcer/restrictimports/analyze/PackagePatternSpecifityTest.java#L34) file to have a glance at how _specificity_ works.


## Static imports
Matching static imports is also possible but the `static ` prefix must be explicitly mentioned:
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <bannedImport>static org.junit.Assert.*</bannedImport>
        </restrictImports>
    </rules>
</configuration>
```
Inclusions and exclusion will work identically.

## Test code
By default, test code is not subject to the banned import checks. You can enable analysis
of test code using the `includeTestCode` option.
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <includeTestCode>true</includeTestCode>
            <!-- ... -->
        </restrictImports>
    </rules>
</configuration>
```

## Skipping
Using the configuration option `skip` you are able to temporarily disable a rule 
instance. 
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <skip>true</skip>
            <!-- ... -->
        </restrictImports>
    </rules>
</configuration>
```
If you want banned import analysis but without breaking your build you can set
`failBuild` to `false`.
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <failBuild>false</failBuild>
            <!-- ... -->
        </restrictImports>
    </rules>
</configuration>
```

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
correct. If a source file is not, the analysis result is undefined.

### Conceptual limitation
Import recognition works by comparing the import statements within your source files 
against the specified patterns. If your class uses wildcard imports like in

```java
import java.util.*;
```

this plugin will not be able to match that import against a banned pattern pointing to a 
concrete class like `java.util.ArrayList`. However, wildcard recognition would still work
as expected.

For checking the `basePackage` and `exclusion` patterns, the plugin tries to construct the
_full qualified class name_ (FQCN) of each analyzed source file. It does so by 
concatenating the file name to the source file's value of the `package <value>;` 
statement. Thus if your `exclusion` pattern points to a concrete class like 
`com.name.ClassName` the exclusion will only match if this class is declared in a file 
with the exact name `ClassName.java`. The same applies in case you use a base package 
pattern with no wild cards.


## Configuration options

Overview of all configuration parameters:

| Parameter               | Type                      | Required | Default                           | Since    |
|-------------------------|---------------------------|----------|-----------------------------------|----------|
| `basePackage(s)`        | (List of) package pattern | no       | `**`                              |          |
| `bannedImport(s)`       | (List of) package pattern | yes      |                                   |          |
| `allowedImport(s)`      | (List of) package pattern | no       | empty list                        |          |
| `exclusion(s)`          | (List of) package pattern | no       | empty list                        |          |
| `includeTestCode`       | Boolean                   | no       | `false`                           | `0.7.0`  |
| `reason`                | String                    | no       | empty String                      | `0.8.0`  |
| `failBuild`             | Boolean                   | no       | `true`                            | `0.17.0` |
| `skip`                  | Boolean                   | no       | `false`                           | `0.17.0` |
| `includeCompileCode`    | Boolean                   | no       | `true`                            | `1.2.0` |

* _Deprecated_: Setting this property might have no effect but will log a descriptive warning
* _Soft-Removed_: Setting this property will fail the build with a descriptive warning that this property is no longer supported
* _Removed_: The property no longer exists and the plugin behaves as if it never did.

## Versioning and Compatibility
This project adheres to version 2 of the [semantic version specification](http://semver.org).

You can always safely update the _minor_ and the _patch_ version of the rule's dependency entry within a pom.xml without 
breaking your build. Interface or behavioral changes will only ever be introduced with a new _major_ version. Changes
that break previous plugin configurations that were wrong in the first place may also be introduced with 
a _minor_ version change!

This artifact is (currently) not meant to be used as standalone dependency. Thus breaking code changes might occur 
even between two different patch versions!

## Changelog

### Version 1.2.0
* [44](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/44): Bump guava to `30.1.1-jre`
* [43](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/43): Allow to run on test code only

### Version 1.1.0
* [42](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/42): Add some more configuration consistency checks
* Update section about _Versioning and Compatibility_

### Version 1.0.1
* [#39](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/39): Handle double semicolon after import statement
* [#37](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/37): Fix version incompatibility
* Display analysis duration

### Version 1.0.0
* [#35](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/35): Recognize multiple import statements on same line in groovy sources 
* _Remove_ `commentLineBufferSize`

### Version 0.18.0
* Fix possible file resource leak while iterating source files
* _Remove_ `sourceFileCharset`
* _Soft-Remove_ `commentLineBufferSize`
* [#34](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/34): Recognize multiple import statements on same line in java sources
* [#33](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/33): Possibility to forbid wildcard imports
* [#31](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/31): Results show whether a match occurred in a test file
* [#30](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/30): Separate import parsing and banned import analysis

### Version 0.17.0
* [#29](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/29): SPI for `SourceLineParser` implementations
* [#27](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/27): Introduce `failBuild` and `skip` options
* Fix mysterious case where `"*` would be recognized as the beginning of a block comment
* Introduce changelog to readme
* Add [contribution guide lines](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/master/CONTRIBUTING.md)

### Version 0.16.0
* [#26](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/26): _Deprecate_ `commentLineBufferSize` parameter and replaced with dynamically growing buffer
* [#25](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/25): Add additional configuration sanity checks
* [#23](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/23): _Soft-Remove_ `sourceFileCharset` parameter and always use `${project.build.sourceEncoding}` now
