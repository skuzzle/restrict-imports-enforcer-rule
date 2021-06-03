### Forbid all wildcard imports
```xml
<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
    <reason>Don't use wildcard imports</reason>
    <bannedImport>**.'*'</bannedImport>
</restrictImports>
```
_Note_: `'*'` is a special literal which is treated specially. It is only allowed as the last part of a package 
pattern.

### Forbid all static imports
```xml
<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
    <reason>Don't use static imports</reason>
    <bannedImport>static **</bannedImport>
</restrictImports>
```


### Unify logging frameworks
```xml
<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">
    <reason>Use slf4j Logger</reason>
    <bannedImport>**.Logger</bannedImport>
    <allowedImport>org.slf4j.Logger</allowedImport>
</restrictImports>
```