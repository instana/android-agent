# Instana agent for Flutter

**[Changelog](CHANGELOG.md)** |
**[Contributing](CONTRIBUTING.md)**

---

Instana agent allows Flutter apps to send custom traces to Instana. 

Traces should instantly appear in your Instana dashboard. 

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

## Tracing View changes

At any point after initialing Instana agent:

```dart
import 'package:instana_agent/instana_agent.dart';

[...]

InstanaAgent.setView('Home');
```

## Tracing Http requests

At any point after initialing Instana agent:

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
class _InstrumentedHttpClient extends http.BaseClient {
  _InstrumentedHttpClient(this._inner);

  final http.Client _inner;

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) async {
    final Marker marker = Instana.startCapture(url: request.url.toString(), method: request.method);
    
    StreamedResponse response;
    try {
      response = await _inner.send(request);
      marker
         ..responseStatusCode = response.statusCode
         ..responseSizeBody = response.contentLength;
    } finally {
      await marker.finish();
    }

    return response;
  }
}

class _MyAppState extends State<MyApp> {

  [...]
  
  Future<void> httpRequest() async {
    final _InstrumentedHttpClient httpClient = _InstrumentedHttpClient(http.Client());
    final Request request = Request("GET", Uri.parse("https://www.instana.com"));
    httpClient.send(request);
  }

  [...]
}
```

## More

The complete documentation for this package, including `custom events` and such can be found in: [Instana Flutter API](https://www.instana.com/docs/mobile_app_monitoring/flutter_api) 

Please also check out the [Flutter example](https://github.com/instana/flutter-agent/tree/main/example) in this repository for a simple usage demonstration.
