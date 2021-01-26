# Contributing

## Building Instana Agent for Flutter

The example app included in this project will always use the `instana_agent` package contained in this repository. 

The steps to build the code into a package and run the example with it remain as simple as:

1. Install [flutter](https://flutter.dev/docs/get-started/install) 
2. Clone this repository
3. Go to the example folder
4. Use the terminal to start flutter with `flutter run`

## Release Process

We follow [Semantic Versioning 2.0](https://semver.org/).

Steps:
1. Update [CHANGELOG.md](./CHANGELOG.md) with the new version
2. Update [pubspec.yaml](./pubspec.yaml) with the new version
3. Commit and push the change
4. Run `flutter pub publish --dry-run` to verify all is good
5. Run `flutter pub publish --dry-run` to publish


For more info, please check the [Flutter docs for Publishing Packages](https://flutter.dev/docs/development/packages-and-plugins/developing-packages#publish) and the [Dart docs for Publishing Packages](https://dart.dev/tools/pub/publishing).
