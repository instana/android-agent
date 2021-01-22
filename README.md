# Instana agent for Flutter

**[Changelog](CHANGELOG.md)** |
**[Contributing](CONTRIBUTING.md)**

---

## Installation

The package **Instana agent** for Flutter is available via pub.dev.

1. Open the `pubspec.yaml` file located inside the app folder, and add `instana_agent`: under dependencies.
2. Install it
From the terminal: Run `flutter pub get`
Or
    * From Android Studio/IntelliJ: Click Packages get in the action ribbon at the top of pubspec.yaml.
    * From VS Code: Click Get Packages located in right side of the action ribbon at the top of pubspec.yaml.

## Usage

Import package in your dart file(s)
```dart
import 'package:instana_agent/instana_agent.dart';
```
Stop and restart the app, if necessary

## Setup
Setup Instana via `initState`

```dart
@override
  void initState() {
    super.initState();
    setupInstana();
  }

Future<void> setupInstana() async {
    await InstanaAgent.setup(key: 'YOUR-INSTANA-KEY', reportingUrl: 'YOUR-REPORTING_URL');
    // For further calls and functions see the documentation
}
```
For the whole documentation see the [Instana Flutter API](https://www.instana.com/docs/mobile_app_monitoring/flutter_api) 
You can also check out the [Flutter example](https://github.com/instana/flutter-agent/tree/main/example) in this repository.
