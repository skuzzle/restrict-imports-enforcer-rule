# restrict-imports-enforcer-rule
Maven enforcer rule that bans certain imports

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
            <version>0.1.0</version>
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