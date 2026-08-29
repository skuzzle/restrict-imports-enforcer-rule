# Contributing

**Contributions are highly appreciated!**

If you have any questions, feature requests or suspected bugs please do not hesitate to
open a new issue.

If you want to actively contribute code, please follow this advice:
I don't want you to waste your time on changes that I might decline, so please open a new
issue before implementing any new feature and sending a pull request. I'm happy to
discuss your idea and give advice on how to best implement it.

If your idea is accepted, simply send the PR targeting the *development* branch.

## Local Development

### Building
For fast feedback during development you can run
1. `./gradlew quickCheck` or its short-form `./gradlew qC` for fast superficial tests
2. `./gradlew test` for only running quick unit tests
3. `./gradlew functionalTest` for only running slow but comprehensive functional tests
4. `./gradlew check` for running the full test suite

Compiling and testing requires Java 17+

### Code Style
Note that code formatting is enforced by `spotless`. You can apply the configured
formatting rules to your changes by running `./gradlew spotlessApply`

### Build Scans
This project uses the Community Develocity instance provided by
[Develocity.ai](https://develocity.ai) at https://community.develocity.cloud for Build
Scans and remote build caching.
