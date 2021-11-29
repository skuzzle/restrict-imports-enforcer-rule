<!-- This file is auto generated during release from readme/README.md -->

[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.0.0-SNAPSHOT&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.0.0-SNAPSHOT/jar)
[![JavaDoc](https://img.shields.io/static/v1?label=JavaDoc&message=2.0.0-SNAPSHOT&color=orange)](http://www.javadoc.io/doc/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.0.0-SNAPSHOT)
[![Coverage Status](https://coveralls.io/repos/github/skuzzle/restrict-imports-enforcer-rule/badge.svg?branch=master)](https://coveralls.io/github/skuzzle/restrict-imports-enforcer-rule?branch=master)
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
    <version>3.0.0</version>
    <dependencies>
        <dependency>
            <groupId>de.skuzzle.enforcer</groupId>
            <artifactId>restrict-imports-enforcer-rule</artifactId>
            <version>2.0.0-SNAPSHOT</version>
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

# Contents
* [Rationale](#rationale)
* Usage
  * [Includes and Excludes](#includes-and-excludes)
  * [Rule groups](#rule-groups)
  * [Static imports](#static-imports)
  * [Test code](#test-code)
  * [Skipping](#skipping)
  * [Exclude source roots](#exclude-source-roots)
  * [Parallel analysis](#parallel-analysis)
  * [Package patterns](#package-patterns)
* [Limitation](#limitation)
  * [Syntactical](#syntactical-limitation)
  * [Conceptual](#conceptual-limitation)
* [Configuration options](#configuration-options)
* [Versioning and Compatibility](#versioning-and-compatibility)

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
operator but still allow some concrete classes:

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

It is possible to exclude certain source files from being affected by the bans at 
all. You can use `basePackage` to specify a package pattern of classes that are affected 
by the rule. You may then exclude some classes to refine the matches using the
`exclusion` tag. It is also possible to specify multiple base packages.

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

Wherever you write package patterns you can also specify a list of patterns. Thus it is 
possible to define multiple banned imports/exclusions/allowed imports or base packages.

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

## Rule groups
Rule groups add another level of refining which imports will be matched. You can group
the `bannedImport(s)`, `allowedImport(s)` and `basePackage(s)` tags and specify multiple 
of this groups within a single enforcer rule. 

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


When analysing a source file, the plugin filters all groups where the group's 
`basePackage` matches the source file's package name. In case multiple groups are 
matching, only the group with the _most specific_ base package is retained and the others 
are ignored for this file. Have a look at [this](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/develop/src/test/java/de/skuzzle/enforcer/restrictimports/analyze/PackagePatternSpecifityTest.java#L34) file to have a glance at how _specificity_ works.

In the above example, the first group is chosen by default (as by `basePackage=**`) unless a class is matched by the
more specific `basePackage` of the second group. In that case, only the definitions from the second group apply to this 
class.

## Static imports
_Note: Behavior has been changed since version `2.0.0`_

By default, all defined package patterns will automatically match a respective static import. You can revert to the 
"old" (pre version 2.0.0) behavior, where `static` must be explicitly mentioned in the package pattern, by setting the 
flag `includeStaticImports`:
```xml
<configuration>
    <rules>
        <RestrictImports>
            <includeStaticImports>false</includeStaticImports>
            <bannedImport>static org.junit.Assert.*</bannedImport>
        </RestrictImports>
    </rules>
</configuration>
```

## Test code
By default, test code is also subject to the banned import checks (this is new since version `2.0.0`). You can disable 
analysis of test code using the `includeTestCode` option.
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

## Skipping
Using the configuration option `skip` you are able to temporarily disable a rule 
instance. 
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
If you want banned import analysis but without breaking your build you can set
`failBuild` to `false`.
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

You can also pass these parameters as property to the maven build using `-Drestrictimports.skip` resp. 
`-Drestrictimports.failBuild`. When passed as property, the property's value takes precedence over what has been
configured in the pom file.

## Exclude source roots
By default, all source roots reported by Maven is subject to the banned import checks, which for example includes but
is not limited to `${project.basedir}/src/main/java`, `${project.basedir}/src/test/java`,
`${project.build.directory}/generated-sources/main/java` and
`${project.build.directory}/generated-test-sources/main/java`. You can exclude source root(s) using the
`excludedSourceRoot(s)` option, either absolute or relative path.
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

## Parallel Analysis
(*Note:* This is a beta feature and not thoroughly tested. Syntax and behavior 
changes in upcoming versions are likely)

We support basic parallelization of the analysis. This is disabled by default but can be enabled either in the pom file
using the `<parallel>` option or by passing `-Drestrictimports.parallel` to the maven build.
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
| `includeCompileCode`    | Boolean                   | no       | `true`                            | `1.2.0`  |
| `excludedSourceRoot(s)` | (List of) java.io.File    | no       | empty list                        | `1.3.0`  |
| `includeStaticImports`  | Boolean                   | no       | `true`                            | `2.0.0`  |

## Versioning, Deprecations and Compatibility
This project adheres to version 2 of the [semantic version specification](http://semver.org) with regards to the 
plugin's configuration syntax and analysis semantics.

You can always safely update the _minor_ and the _patch_ version of the rule's dependency entry within a pom.xml without 
breaking your build. Breaking interface or behavioral changes will only ever be introduced with a new _major_ version. 

When deprecating/removing functionality, we use the following terminology:
* _Deprecated_: Using this feature still works, but will log a descriptive deprecation warning
* _Soft-Removed_: Using this feature will fail the build with a descriptive warning that this feature is no longer supported
* _Removed_: The feature no longer exists and the plugin behaves as if it never did.

This artifact is not meant to be used as standalone dependency. Thus its actual implementation is not covered by 
semantic versioning.
