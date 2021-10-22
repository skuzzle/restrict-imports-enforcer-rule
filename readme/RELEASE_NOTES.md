[![Maven Central](https://img.shields.io/static/v1?label=MavenCentral&message=${project.version}&color=blue)](https://search.maven.org/artifact/${project.groupId}/${project.artifactId}/${project.version}/jar) [![JavaDoc](https://img.shields.io/static/v1?label=JavaDoc&message=${project.version}&color=orange)](http://www.javadoc.io/doc/${project.groupId}/${project.artifactId}/${project.version})

### Features
* Allow simple declaration as rule via `<RestrictImports>` instead of requiring fully qualified class name.
* Build and test against `enforcer-api:3.0.0` (coming from `3.0.0-M1`) 

### Deprecations
* Deprecated declaration as `<restrictImports implementation="de.skuzzle.enforcer.restrictimports.rule.RestrictImports">` in favor of `<RestrictImports>`

Maven Central coordinates for this release:

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
</dependency>
```