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
            <version>0.7.0</version>
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
                        <!-- prohibit use of java loggers in every source file -->
                        <basePackage>**</basePackage>
                        <!-- Since 0.7.0: Whether to also analyze test code. Defaults to false -->
                        <includeTestCode>true</includeTestCode>
                        <bannedImports>
                            <bannedImport>java.util.logging.**</bannedImport>
                        </bannedImports>
                    </restrictImports>
                </rules>
            </configuration>
        </execution>
        
        <!-- You could have another execution here for restricting further imports -->
        
    </executions>
</plugin>
```

## Package Patterns

Package patterns are matched case sensitively part by part. There are two supported 
wildcard operators:

1. `*` matches a single package part.
2. `**` matches any remaining parts and may only be applied at the end of a pattern.

The pattern `java.util.*` matches `java.util.ArrayList` but not `java.util.regex.Pattern`.

Likewise the pattern `java.util.**` matches all classes and subclasses contained in 
`java.util`.

If a pattern does not contain any wildcards matching degrades to a simple String 
comparison.

## Includes and Excludes
To refine the classes that are banned you may use the `allowedImports` tag in addition to 
the `bannedImports` tag. For example you can exclude a whole sub package using a wildcard
operator and then include some concrete classes:

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <basePackage>**</basePackage>
            <bannedImports>
                <bannedImport>java.util.logging.**</bannedImport>
            </bannedImports>
            <allowedImports>
                <allowedImport>java.util.logging.Handler</allowedImport>
            </allowedImports>
        </restrictImports>
    </rules>
</configuration>
```

It is possible to exclude certain source files from being affected by the bans at 
all. You can use `basePackage` to specify a package pattern of classes that are affected 
by the rule. You may then exclude some classes to refine the matches using the
`excludedClasses` tag.

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <basePackage>com.your.domain.**</basePackage>
            <bannedImports>
                <bannedImport>java.util.logging.**</bannedImport>
            </bannedImports>
            <allowedImports>
                <allowedImport>java.util.logging.Handler</allowedImport>
            </allowedImports>
            <excludedClasses>
                <excludedClass>com.your.domain.treat.special.*</excludedClass>
            </excludedClasses>
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
            <basePackage>com.your.domain.**</basePackage>
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

| Parameter         | Type                    | Required | Default    | Since   |
|-------------------|-------------------------|----------|------------|---------|
| `basePackage`     | Package pattern         | no       | `**`       |         |
| `bannedImports`   | List of package pattern | yes      | -          |         |
| `allowedImports`  | List of package pattern | no       | empty list |         |
| `excludedClasses` | List of package pattern | no       | empty list |         |
| `includeTestCode` | Boolean                 | no       | `false`    | `0.7.0` |
