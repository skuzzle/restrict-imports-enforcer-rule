[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=1.4.1-SNAPSHOT&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/1.4.1-SNAPSHOT/jar) [![JavaDoc](https://img.shields.io/static/v1?label=JavaDoc&message=1.4.1-SNAPSHOT&color=orange)](http://www.javadoc.io/doc/de.skuzzle.enforcer/restrict-imports-enforcer-rule/1.4.1-SNAPSHOT)

### Features
* Allow simple declaration as rule via `<RestrictImports>` instead of requiring fully qualified class name.
* Build and test against `enforcer-api:3.0.0` (coming from `3.0.0-M1`) 

### Deprecations
* Deprecated full qualified declaration as `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">` 
in favor of just `<RestrictImports>`. Using the deprecated declaration will print a warning during the build. The 
deprecated declaration format will be removed with the next major release, that is `2.x.x`

### Otherwise Noteworthy
* [#38](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/38) Removed Guava dependency altogether.
* Removed the changelog from the main README. Notable changes for each release are now attached directly to each GitHub 
release. Existing release notes up to version `1.3.0` can still be found in `CHANGELOG_LEGACY.md` file.

Maven Central coordinates for this release:

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>1.4.1-SNAPSHOT</version>
</dependency>
```