import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_agent/flutter_agent.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    setupInstana();
  }

  Future<void> setupInstana() async {
    await FlutterAgent.setup(key: 'KEY', reportingUrl: 'REPORTING_URL');
    await FlutterAgent.setUserID('1234567890');
    await FlutterAgent.setUserName('Boty McBotFace');
    await FlutterAgent.setUserEmail('boty@mcbot.com');
    await FlutterAgent.setView('Home');
    await FlutterAgent.setMeta(key: 'exampleGlobalKey', value: 'exampleGlobalValue');
    await FlutterAgent.reportEvent(name: 'simpleCustomEvent');
    await FlutterAgent.reportEvent(
        name: 'complexCustomEvent',
        options: EventOptions()
          ..viewName = 'customViewName'
          ..startTime = DateTime.now().millisecondsSinceEpoch
          ..duration = 2 * 1000);
    await FlutterAgent.reportEvent(
        name: 'advancedCustomEvent',
        options: EventOptions()
          ..viewName = 'customViewName'
          ..startTime = DateTime.now().millisecondsSinceEpoch
          ..duration = 3 * 1000
          ..meta = {'customKey1': 'customValue1', 'customKey2': 'customValue2'});
    await FlutterAgent.startCapture(url: 'https://example.com/success', method: 'GET').then((marker) => marker
      ..responseStatusCode = 200
      ..responseSizeBody = 1000
      ..responseSizeBodyDecoded = 2400
      ..finish());
    await FlutterAgent.startCapture(url: 'https://example.com/cancel', method: 'POST').then((marker) => marker.cancel());
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterAgent.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }
}
