
This changelog is no longer maintained. Follow the release notes at the GitHub releases for latest changes

## Changelog

### Version 1.3.0
* [47](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/47): Allow to exclude source roots

### Version 1.2.0
* [44](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/44): Bump guava to `30.1.1-jre`
* [43](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/43): Allow to run on test code only

### Version 1.1.0
* [42](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/42): Add some more configuration consistency checks
* Update section about _Versioning and Compatibility_

### Version 1.0.1
* [#39](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/39): Handle double semicolon after import statement
* [#37](https://github.com/skuzzle/restrict-imports-enforcer-rule/pull/37): Fix version incompatibility
* Display analysis duration

### Version 1.0.0
* [#35](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/35): Recognize multiple import statements on same line in groovy sources
* _Remove_ `commentLineBufferSize`

### Version 0.18.0
* Fix possible file resource leak while iterating source files
* _Remove_ `sourceFileCharset`
* _Soft-Remove_ `commentLineBufferSize`
* [#34](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/34): Recognize multiple import statements on same line in java sources
* [#33](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/33): Possibility to forbid wildcard imports
* [#31](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/31): Results show whether a match occurred in a test file
* [#30](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/30): Separate import parsing and banned import analysis

### Version 0.17.0
* [#29](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/29): SPI for `SourceLineParser` implementations
* [#27](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/27): Introduce `failBuild` and `skip` options
* Fix mysterious case where `"*` would be recognized as the beginning of a block comment
* Introduce changelog to readme
* Add [contribution guide lines](https://github.com/skuzzle/restrict-imports-enforcer-rule/blob/master/CONTRIBUTING.md)

### Version 0.16.0
* [#26](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/26): _Deprecate_ `commentLineBufferSize` parameter and replaced with dynamically growing buffer
* [#25](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/25): Add additional configuration sanity checks
* [#23](https://github.com/skuzzle/restrict-imports-enforcer-rule/issues/23): _Soft-Remove_ `sourceFileCharset` parameter and always use `UTF-8` now
