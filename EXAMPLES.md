### Forbid all wildcard imports
```xml
<RestrictImports>
    <reason>Don't use wildcard imports</reason>
    <bannedImport>**.'*'</bannedImport>
</RestrictImports>
```
_Note_: `'*'` is a special literal which is treated specially. It is only allowed as the last part of a package 
pattern.

### Forbid all static imports
```xml
<restrictImports>
    <reason>Don't use static imports</reason>
    <!-- 
        This flag is required since version 2.0.0. 
        Otherwise the pattern 'static **' would be semantically equivalent to '**' and thus forbid every single import.
    -->
    <includeStaticImports>false</includeStaticImports>
    <bannedImport>static **</bannedImport>
</RestrictImports>
```


### Unify logging frameworks
```xml
<RestrictImports>
    <reason>Use slf4j Logger</reason>
    <bannedImport>**.Logger</bannedImport>
    <allowedImport>org.slf4j.Logger</allowedImport>
</RestrictImports>
```