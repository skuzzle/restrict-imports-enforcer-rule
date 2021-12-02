[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=2.0.0&color=blue)](https://search.maven.org/artifact/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.0.0/jar) [![JavaDoc](https://img.shields.io/static/v1?label=JavaDoc&message=2.0.0&color=orange)](http://www.javadoc.io/doc/de.skuzzle.enforcer/restrict-imports-enforcer-rule/2.0.0)

This is a new major release. Upgrading the version _might_ break your build if you don't adjust your configuration 
according to the changes mentioned below. Please carefully read the _changes_ section when you are upgrading from `1.x.y`.

### Changes
(_Note:_ Review carefully, as those might break your existing build when updating from version `1.x.y`)
 
* [#28](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/28) Test code is always analyzed unless `<includeTestCode>` option is configured `false`.
* [#49](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/49) Simplify and formalize _"pattern specificity"_.
* [#53](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/53) Package patterns now implicitly match static imports.
* Declaration variant no longer supported: `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">` (deprecated since `0.12.0`).
* Declaration variant no longer supported: `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">` (deprecated since `1.4.0`).

### Features
* [#50](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/50) Introduce `-Drestrictimports.skip` and `-Drestrictimports.failBuild` command line options.
* Added `<parallel>` (`-Drestrictimports.parallel`) option to run the whole analysis in parallel (Experimental feature, feedback welcome).
* Improve formatting of the analysis result.

### Bug fixes
* [#52](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/52) Whitespaces in plugin configuration and source files are handled more gracefully.


Maven Central coordinates for this release:

```xml
<dependency>
    <groupId>de.skuzzle.enforcer</groupId>
    <artifactId>restrict-imports-enforcer-rule</artifactId>
    <version>2.0.0</version>
</dependency>
```