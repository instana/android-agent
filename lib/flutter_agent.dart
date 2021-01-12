import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class FlutterAgent {
  static const MethodChannel _channel = const MethodChannel('flutter_agent');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> setup({@required String key, @required String reportingUrl}) async {
    return await _channel.invokeMethod('setup', <String, dynamic>{'key': key, 'reportingUrl': reportingUrl});
  }

  static Future<void> setUserID(String userID) async {
    await _channel.invokeMethod('setUserID', <String, dynamic>{'userID': userID});
  }

  static Future<void> setUserName(String name) async {
    await _channel.invokeMethod('setUserName', <String, dynamic>{'userName': name});
  }

  static Future<void> setUserEmail(String email) async {
    await _channel.invokeMethod('setUserEmail', <String, dynamic>{'userEmail': email});
  }

  static Future<void> setView(String name) async {
    await _channel.invokeMethod('setView', <String, dynamic>{'viewName': name});
  }

  static Future<void> setMeta({@required String key, @required String value}) async {
    await _channel.invokeMethod('setMeta', <String, dynamic>{'key': key, 'value': value});
  }

  static Future<void> setIgnore(List<String> urls) async {
    await _channel.invokeMethod('setIgnore', <String, dynamic>{'urls': urls});
  }

  static Future<void> setIgnoreRegex(List<RegExp> regex) async {
    var regexString = regex.map((val) => val.toString()).toList();
    await _channel.invokeMethod('setIgnoreRegex', <String, dynamic>{'regex': regexString});
  }

  static Future<void> reportEvent({@required String name, EventOptions options}) async {
    await _channel.invokeMethod('reportEvent', <String, dynamic>{
      'eventName': name,
      'startTime': options?.startTime?.toDouble(),
      'duration': options?.duration?.toDouble(),
      'viewName': options?.viewName,
      'meta': options?.meta,
      'backendTracingID': options?.backendTracingID
    });
  }

  static Future<Marker> startCapture({@required String url, String viewName}) async {
    var markerId, view = await _channel.invokeMethod('startCapture', <String, dynamic>{'url': url, 'viewName': viewName});
    return Marker(channel: _channel, id: markerId, viewName: view);
  }
}

class Marker {
  Marker({@required MethodChannel channel, @required this.id, @required this.viewName}) : _channel = channel;

  final MethodChannel _channel;
  final String id;

  String viewName;
  int responseStatusCode = -1;
  String method;
  String backendTracingID;
  int responseSizeHeader;
  int responseSizeBody;
  int responseSizeBodyDecoded;
  String errorMessage;

  Future<void> finish() async {
    await _channel.invokeMethod('finish', <String, dynamic>{
      'id': id,
      'responseStatusCode': responseStatusCode,
      'method': method,
      'backendTracingID': backendTracingID,
      'responseSizeHeader': responseSizeHeader,
      'responseSizeBody': responseSizeBody,
      'responseSizeBodyDecoded': responseSizeBodyDecoded,
      'errorMessage': errorMessage
    });
  }
}

class EventOptions {
  int startTime;
  int duration;
  String viewName;
  Map<String,String> meta;
  String backendTracingID;
}
