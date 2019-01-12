[![Build Status](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule.svg?branch=master)](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule)
[![Coverage Status](https://coveralls.io/repos/skuzzle/restrict-imports-enforcer-rule/badge.svg?branch=master&service=github)](https://coveralls.io/github/skuzzle/restrict-imports-enforcer-rule?branch=master)


# restrict-imports-enforcer-rule
Maven enforcer rule that bans certain imports. Keep your code base clean and free from 
usage of unwanted classes!
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
    <version>3.0.0-M2</version>
    <dependencies>
        <dependency>
            <groupId>de.skuzzle.enforcer</groupId>
            <artifactId>restrict-imports-enforcer-rule</artifactId>
            <version>0.17.0</version>
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
                        <!-- Since 0.8.0: Define an explanatory reason why these imports are prohibited -->
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
`excludedClasses` tag. It is also possible to specify multiple base packages.

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
            <allowedImports>
            <!-- ... -->
        </restrictImports>
    </rules>
</configuration>
```

## Rule groups (beta, since 0.13.0)
(*Note:* This is a beta feature and not thoroughly tested. Syntax and behavior 
changes in upcoming versions are likely)

Rule groups add another level of refining which imports will be matched. You can group
the `bannedImport(s)`, `allowedImport(s)` and `basePackage(s)` tags and specify multiple 
of this groups within a single enforcer rule. 

(example stolen from #6)
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
            <groups>
                <group>
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
are ignored for this file.


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

Package patterns are matched case sensitively part by part. There are two supported 
wildcard operators:

1. `*` matches every package part but exactly one.
2. `**` matches multiple package parts but at least one.

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
| `failBuild`             | Boolean                   |          | `true`                            | `0.17.0` |
| `skip`                  | Boolean                   |          | `false`                           | `0.17.0` |
| `commentLineBufferSize` | Integer                   | no       | 128                               | `0.11.0` (deprecated in `0.16.0`) |
| `sourceFileCharset`     | String                    | no       | `${project.build.sourceEncoding}` | `0.11.0` (deprecated in `0.15.0`, removed in `0.16.0`) |

## Changelog

### Version 0.17.0
* [#29](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/29): SPI for `SourceLineParser` implementations
* [#27](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/27): Introduce `failBuild` and `skip` options
* Fix mysterious case where `"*` would be recognized as the beginning of a block comment
* Introduce changelog to readme
* Add [contribution guide lines](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/master/CONTRIBUTING.md)

### Version 0.16.0
* [#26](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/26): Deprecated `commentLineBufferSize` parameter
* [#25](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/25): Add additional configuration sanity checks
* [#23](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/23): Removed deprecated `sourceFileCharset` parameter
