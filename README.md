# Instana agent for Flutter

**[Changelog](CHANGELOG.md)** |
**[Contributing](CONTRIBUTING.md)**

---

Instana agent allows Flutter apps to send monitoring data to Instana. 

## Installation

The **Instana agent** Flutter package is available via [pub.dev](https://pub.dev/). 

You can add it to your app the same way as usual:

1. Open the `pubspec.yaml` file located inside the app folder, and add `instana_agent:` under `dependencies`.
2. Install it
From the terminal: Run `flutter pub get`
Or
    * From Android Studio/IntelliJ: Click **Packages get** in the action ribbon at the top of `pubspec.yaml`.
    * From VS Code: Click **Get Packages** located in right side of the action ribbon at the top of `pubspec.yaml`.

## Initialization

Import package in your dart file(s):

```dart
import 'package:instana_agent/instana_agent.dart';
```

Stop and restart the app, if necessary

Setup Instana once as soon as possible. For example, in `initState()`

```dart
@override
  void initState() {
    super.initState();
    InstanaAgent.setup(key: 'YOUR-INSTANA-KEY', reportingUrl: 'YOUR-REPORTING_URL');
  }
```

## Tracking View changes

At any point after initializing the Instana agent:

```dart
import 'package:instana_agent/instana_agent.dart';

[...]

InstanaAgent.setView('Home');
```

## Tracking HTTP requests

At any point after initializing the Instana agent:

```dart
import 'package:instana_agent/instana_agent.dart';

[...]

InstanaAgent.startCapture(url: 'https://example.com/success', method: 'GET').then((marker) => marker
    ..responseStatusCode = 200
    ..responseSizeBody = 1000
    ..responseSizeBodyDecoded = 2400
    ..finish());
```

We recommend creating your own `InstrumentedHttpClient` extending `http.BaseClient` as shown in this snippet, for example:

```dart
class _InstrumentedHttpClient extends BaseClient {
   _InstrumentedHttpClient(this._inner);

   final Client _inner;

   @override
   Future<StreamedResponse> send(BaseRequest request) async {
      final Marker marker = await InstanaAgent.startCapture(url: request.url.toString(), method: request.method);

      StreamedResponse response;
      try {
         response = await _inner.send(request);
         marker
            ..responseStatusCode = response.statusCode
            ..responseSizeBody = response.contentLength
            ..backendTracingID = BackendTracingIDParser.fromHeadersMap(response.headers);
      } finally {
         await marker.finish();
      }

      return response;
   }
}

class _MyAppState extends State<MyApp> {

   [...]

   Future<void> httpRequest() async {
      final _InstrumentedHttpClient httpClient = _InstrumentedHttpClient(Client());
      final Request request = Request("GET", Uri.parse("https://www.instana.com"));
      httpClient.send(request);
   }

   [...]
}
```

## Error handling

All of the agent's interfaces return an asynchronous `Future`. Error are wrapped in an exception of the [PlatformException type](https://api.flutter.dev/flutter/services/PlatformException-class.html).

We advice developers to follow the [common error-handling techniques for Futures](https://dart.dev/guides/libraries/futures-error-handling) and at least log any possible error.

For example:

```dart
InstanaAgent.setup(key: 'KEY', reportingUrl: 'REPORTING_URL')
    .catchError((e) => 
            log("Captured PlatformException during Instana setup: $e")
        );
```

Or similarly in async functions:

```dart
try {
  var result = await InstanaAgent.setup(key: 'KEY', reportingUrl: 'REPORTING_URL');
} catch (e) {
log("Captured PlatformException during Instana setup: $e");
}
```

## More

The complete documentation for this package, including `custom events` and others can be found within [Instana's public documentation page](https://www.instana.com/docs/mobile_app_monitoring/flutter_api) 

Please also check out the [Flutter example](https://github.com/instana/flutter-agent/tree/main/example) in this repository for a simple usage demonstration.
