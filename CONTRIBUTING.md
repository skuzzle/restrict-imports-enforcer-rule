# Contributing

**Contributions are highly appreciated!**

If you have any questions, feature requests or suspected bugs please do not hesitate to
open a new issue.

If you want to actively contribute code, please follow this advice:
I don't want you to waste your time on changes that I might decline, so please open a new
issue before implementing any new feature and sending a pull request. I'm happy to
discuss your idea and give advice on how to best implement it.

If your idea is accepted, simply send the PR targetting the *development* branch.

## Local Development

### Building
For fast feedback during development you can run `./gradlew quickCheck` or its short-form `./gradlew qC`. This runs only fast unit tests as well as code style checks.

To run the full test suite use `./gradlew check`.

### Code Style
Note that code formatting is enforced by `spotless`. You can apply the configured
formatting rules to your changes by running `./gradlew spotlessApply`

### Build Scans
When building the project locally gradle will ask you whether you want to accept the TOS of the public
Build Scan service:
> Publishing a build scan to scans.gradle.com requires accepting the Gradle Terms of Service defined at https://gradle.com/terms-of-service. Do you accept these terms? [yes, no]

You can safely decline this if you do not want to publish a Build Scan.

You can also run `./gradlew acceptToS` to permanently accept the TOS on the current working copy of the project. This task creates a marker file in the root directory of the project. As long as this file exists, Build Scans will be published without further asking for consent. You can remove the marker file at any time.
