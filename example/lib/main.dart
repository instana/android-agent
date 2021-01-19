import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter/services.dart';
import 'dart:math';
import 'package:flutter_agent/flutter_agent.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Future<Album> futureAlbum;
  @override
  void initState() {
    super.initState();
    setupInstana();
    futureAlbum = fetchAlbum();
  }

  Future<void> setupInstana() async {
    await FlutterAgent.setup(key: 'KEY', reportingUrl: 'URL');
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
          ..meta = {
            'customKey1': 'customValue1',
            'customKey2': 'customValue2'
          });
    await FlutterAgent.startCapture(url: 'https://example.com/success', method: 'GET')
        .then((marker) => marker
          ..responseStatusCode = 200
          ..responseSizeBody = 1000
          ..responseSizeBodyDecoded = 2400
          ..finish());
    await FlutterAgent.startCapture(url: 'https://example.com/cancel', method: 'POST')
        .then((marker) => marker.cancel());
  }

  Future<Album> fetchAlbum() async {
    Random random = new Random();
    var id = random.nextInt(100);
    var url = 'https://jsonplaceholder.typicode.com/albums/' + id.toString();
    var marker = await FlutterAgent.startCapture(url: url, method: 'GET', viewName: 'Album');
    final response = await http.get(url);
    marker.responseStatusCode = response.statusCode;
    marker.finish();
    if (response.statusCode == 200) {
      // If the server did return a 200 OK response,
      // then parse the JSON.
      return Album.fromJson(jsonDecode(response.body));
    } else {
      // If the server did not return a 200 OK response,
      // then throw an exception.
      throw Exception('Failed to load album');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              FlatButton(
                  color: Colors.blue,
                  textColor: Colors.white,
                  padding: EdgeInsets.all(8.0),
                  splashColor: Colors.blueAccent,
                  onPressed: () {
                    this.setState(() {
                      futureAlbum = fetchAlbum();
                    });
                  },
                  child: Text("Reload")),
              FutureBuilder<Album>(
                future: futureAlbum,
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return Text("Title: " +
                        snapshot.data.title +
                        "\nID: " +
                        snapshot.data.id.toString());
                  } else if (snapshot.hasError) {
                    return Text("${snapshot.error}");
                  } else {
                    return Text("Loading...");
                  }
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}

class Album {
  final int userId;
  final int id;
  final String title;

  Album({this.userId, this.id, this.title});

  factory Album.fromJson(Map<String, dynamic> json) {
    return Album(
      userId: json['userId'],
      id: json['id'],
      title: json['title'],
    );
  }
}
