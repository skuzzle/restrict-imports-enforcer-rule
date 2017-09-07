[![Build Status](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule.svg?branch=master)](https://travis-ci.org/skuzzle/restrict-imports-enforcer-rule) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.skuzzle.enforcer/restrict-imports-enforcer-rule)
[![Coverage Status](https://coveralls.io/repos/skuzzle/restrict-imports-enforcer-rule/badge.svg?branch=master&service=github)](https://coveralls.io/github/skuzzle/restrict-imports-enforcer-rule?branch=master)

# restrict-imports-enforcer-rule
Maven enforcer rule that bans certain imports. Available from Maven Central.

## Usage

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>1.4.1</version>
    <dependencies>
        <dependency>
            <groupId>de.skuzzle.enforcer</groupId>
            <artifactId>restrict-imports-enforcer-rule</artifactId>
            <version>0.8.0</version>
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
                    <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
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

## Package Patterns

Package patterns are matched case sensitively part by part. There are two supported 
wildcard operators:

1. `*` matches every package part but exactly one.
2. `**` matches multiple package parts but at least one.

The pattern `java.util.*` matches `java.util.ArrayList` but not `java.util.regex.Pattern`.

Likewise the pattern `java.util.**` matches all classes and subclasses contained in 
`java.util`. Double wildcards are now supported everywhere within a pattern. `**.DumbName`
would match every import which ends in `DumbName`.

If a pattern does not contain any wildcards, matching degrades to a simple String 
comparison.

## Includes and Excludes
To refine the classes that are banned you may use the `allowedImports` tag in addition to 
the `bannedImports` tag. For example you can exclude a whole sub package using a wildcard
operator and then include some concrete classes:

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
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
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <basePackages>
                <basePackage>com.your.domain.**</basePackage>
                <basePackage>com.your.company.**</basePackage>
            </basePackages>
            <bannedImport>java.util.logging.**</bannedImport>
            <allowedImport>java.util.logging.Handler</allowedImport>
            <!-- The following packages will not be checked for banned imports -->
            <excludedClass>com.your.domain.treat.special.*</excludedClass>
        </restrictImports>
    </rules>
</configuration>
```

Wherever you write package patterns you can also specify a list of patterns. Thus it is 
possible to define multiple banned imports/exclusions/allowed imports or base packages.

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
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


## Test code
By default, test code is not subject to the banned import checks. You can enable analysis
of test code using the `includeTestCode` option.
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <includeTestCode>true</includeTestCode>
            <!-- ... -->
        </restrictImports>
    </rules>
</configuration>
```


## Limitation
Import recognition works by comparing the import statements within your source files 
against the specified patterns. If your class uses wildcard imports like in

```java
import java.util.*;
```

this plugin will not be able to match that import against a banned pattern pointing to a 
concrete class like `java.util.ArrayList`. However, wildcard recognition would still work
as expected.

Likewise `basePackage` and `excludedClass` patterns will only be matched against a source 
file's file name concatenated to its package. Thus it is not possible to match inner 
classes or classes where the file name is not equal to the containing class name.


## Configuration options

Overview of all configuration parameters:

| Parameter           | Type                      | Required | Default    | Since   |
|---------------------|---------------------------|----------|------------|---------|
| `basePackage(s)`    | (List of) package pattern | no       | `**`       |         |
| `bannedImport(s)`   | (List of) package pattern | yes      |            |         |
| `allowedImport(s)`  | (List of) package pattern | no       | empty list |         |
| `excludedClasse(s)` | (List of) package pattern | no       | empty list |         |
| `includeTestCode`    | Boolean                   | no       | `false`    | `0.7.0` |
| `reason`            | String                    |          | empty      | `0.8.0` |
