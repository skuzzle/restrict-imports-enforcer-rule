[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=${project.version}&color=blue)](https://search.maven.org/artifact/${project.groupId}/${project.artifactId}/${project.version}/jar) [![JavaDoc](https://img.shields.io/static/v1?label=JavaDoc&message=${project.version}&color=orange)](http://www.javadoc.io/doc/${project.groupId}/${project.artifactId}/${project.version})

This is a new major release. Please carefully read the _changes_ section below when you are upgrading from `1.x.x`.

### Changes
* [#28](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/28) When analyzing, include test code by default.
* [#49](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/49) Simplify and formalize _"pattern specificy"_.
* Declaration variant no longer supported: `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.RestrictImports">` (deprecated since `0.12.0`)
* Declaration variant no longer supported: `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">` (deprecated since `1.4.0`)


### Features
* [#50](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/50) Introduce `-Drestrictimports.skip` and `-Drestrictimports.failBuild` command line options.


Maven Central coordinates for this release:

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
</dependency>
```