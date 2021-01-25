# Contributing

## Working on the Flutter agent
1. Install [flutter](https://flutter.dev/docs/get-started/install) 
2. Clone this repository
3. Go to the example folder
4. Use the terminal to start flutter with `flutter run`

## Release Process

To make a release, you first need to ensure that the released version will either be a semver minor or patch release so that automatic updates are working for our users. Following that, the process is simple:
Update CHANGELOG.md so that the unreleased section gets its version number. Commit and push this change.
Run the command `flutter pub publish` to publish the new version.
