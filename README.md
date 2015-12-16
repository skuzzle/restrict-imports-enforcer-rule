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
            <version>0.2.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <rules>
            <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
                <bannedImports>
                    <!-- Ban loggers i.e. to enforce usage of slf4j -->
                    <bannedImport>java.util.logging.**</bannedImport>
                </bannedImports>
            </restrictImports>
        </rules>
    </configuration>
</plugin>
```

## Package Patterns

Package patterns are matched case sensitively part by part. There are two supported 
wildcard operators:

1. `*` matches a single part.
2. `**` matches any remaining parts and may only be applied at the end of a pattern.

So the pattern `java.util.*` matches `java.util.ArrayList` but not 
`java.util.regex.Pattern`.

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
            <bannedImports>
                <!-- Ban loggers i.e. to enforce usage of slf4j -->
                <bannedImport>java.util.logging.**</bannedImport>
            </bannedImports>
            <allowedImports>
                <allowedImport>java.util.logging.Handler</allowedImport>
            </allowedImports>
        </restrictImports>
    </rules>
</configuration>
```

Finally you can exclude whole classes/packages from being analyzed by this rule at all 
using the `excludedClasses` tag:
```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <bannedImports>
                <!-- Ban loggers i.e. to enforce usage of slf4j -->
                <bannedImport>java.util.logging.**</bannedImport>
            </bannedImports>
            <allowedImports>
                <allowedImport>java.util.logging.Handler</allowedImport>
            </allowedImports>
            <excludedClasses>
            </excludedClasses>
        </restrictImports>
    </rules>
</configuration>
```

## Limitation
Import recognition works by comparing the import statements within your source files 
against the specified patterns. If your class uses wildcard imports like in

```java
import java.util.*
```

this plugin will not be able to match that import against a banned pattern pointing to a 
concrete class like `java.util.ArrayList`. However, wildcard recognition would still work
as expected.