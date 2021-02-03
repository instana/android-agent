# Contributing

## How to Contribute

This is an open source project, and we appreciate your help!

In order to clarify the intellectual property license granted with contributions from any person or entity, a Contributor License Agreement ("CLA") must be on file that has been signed by each contributor, indicating agreement to the license terms below. This license is for your protection as a contributor as well as the protection of Instana and its customers; it does not change your rights to use your own contributions for any other purpose.

Please print, fill out, and sign the [contributor license agreement](https://github.com/instana/nodejs-sensor/raw/main/misc/instana-nodejs-cla-individual.pdf). Once completed, please scan the document as a PDF file and email to the following email address: ben@instana.com.

Thank you for your interest in the Instana Flutter project!

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
4. Commit and push the change
5. Create release tag
6. Run `flutter pub publish --dry-run` to verify all is good
7. Run `flutter pub publish` to publish


For more info, please check the [Flutter docs for Publishing Packages](https://flutter.dev/docs/development/packages-and-plugins/developing-packages#publish) and the [Dart docs for Publishing Packages](https://dart.dev/tools/pub/publishing).
