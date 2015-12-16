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
            <version>0.3.0</version>
        </dependency>
    </dependencies>
    <configuration>
        <rules>
            <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
                <bannedImportGroups>
                    <!-- Group that prohibits use of java loggers in every source file -->
                    <bannedImportGroup>
                        <basePackage>**</basePackage>
                        <bannedImports>
                            <bannedImport>java.util.logging.**</bannedImport>
                        </bannedImports>
                    </bannedImportGroup>
                    
                    <!-- Furhter groups -->
                    <bannedImportGroup>
                        <!-- ... -->
                    </bannedImportGroup>
                </bannedImportGroups>
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
To refine the classes that are banned by a certain group you may use the `allowedImports` 
tag in addition to the `bannedImports` tag. For example you can exclude a whole sub package using a wildcard
operator and then include some concrete classes:

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <bannedImportGroups>
                <bannedImportGroup>
                    <basePackage>**</basePackage>
                    <bannedImports>
                        <bannedImport>java.util.logging.**</bannedImport>
                    </bannedImports>
                    <allowedImports>
                        <allowedImport>java.util.logging.Handler</allowedImport>
                    </allowedImports>
                </bannedImportGroup>
            </bannedImportGroups>
        </restrictImports>
    </rules>
</configuration>
```

It is possible to exclude certain source files from being affected by a banned group at 
all. You can use `basePackage` to specify a package pattern of classes that are affected 
by the rule. You may then exclude some classes to refine the matches using the
`excludedClasses` tag.

```xml
<configuration>
    <rules>
        <restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">
            <bannedImportGroups>
                <bannedImportGroup>
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
                </bannedImportGroup>
            </bannedImportGroups>
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