/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class InstanaAgent {
  static const MethodChannel _channel = const MethodChannel('instana_agent');

  static Future<void> setup(
      {@required String key, @required String reportingUrl}) async {
    return await _channel.invokeMethod(
        'setup', <String, dynamic>{'key': key, 'reportingUrl': reportingUrl});
  }

  static Future<String> getSessionID() async {
    return await _channel.invokeMethod('getSessionID', <String, dynamic>{});
  }

  static Future<void> setUserID(String userID) async {
    await _channel
        .invokeMethod('setUserID', <String, dynamic>{'userID': userID});
  }

  static Future<void> setUserName(String name) async {
    await _channel
        .invokeMethod('setUserName', <String, dynamic>{'userName': name});
  }

  static Future<void> setUserEmail(String email) async {
    await _channel
        .invokeMethod('setUserEmail', <String, dynamic>{'userEmail': email});
  }

  static Future<void> setView(String name) async {
    await _channel.invokeMethod('setView', <String, dynamic>{'viewName': name});
  }

  static Future<void> setMeta(
      {@required String key, @required String value}) async {
    await _channel
        .invokeMethod('setMeta', <String, dynamic>{'key': key, 'value': value});
  }

  static Future<void> reportEvent(
      {@required String name, EventOptions options}) async {
    await _channel.invokeMethod('reportEvent', <String, dynamic>{
      'eventName': name,
      'startTime': options?.startTime?.toDouble(),
      'duration': options?.duration?.toDouble(),
      'viewName': options?.viewName,
      'meta': options?.meta,
      'backendTracingID': options?.backendTracingID
    });
  }

  static Future<Marker> startCapture(
      {@required String url, @required String method, String viewName}) async {
    var currentView =
        await _channel.invokeMethod('getView', <String, dynamic>{});
    var markerId = await _channel.invokeMethod(
        'startCapture', <String, dynamic>{
      'url': url,
      'method': method,
      'viewName': viewName ?? currentView
    });
    return Marker(
        channel: _channel, id: markerId, viewName: viewName ?? currentView);
  }
}

class Marker {
  Marker(
      {@required MethodChannel channel,
      @required this.id,
      @required this.viewName})
      : _channel = channel;

  final MethodChannel _channel;
  final String id;
  final String viewName;

  int responseStatusCode;
  String backendTracingID;
  int responseSizeHeader;
  int responseSizeBody;
  int responseSizeBodyDecoded;
  String errorMessage;

  Future<void> finish() async {
    await _channel.invokeMethod('finish', <String, dynamic>{
      'id': id,
      'responseStatusCode': responseStatusCode,
      'backendTracingID': backendTracingID,
      'responseSizeHeader': responseSizeHeader,
      'responseSizeBody': responseSizeBody,
      'responseSizeBodyDecoded': responseSizeBodyDecoded,
      'errorMessage': errorMessage
    });
  }

  Future<void> cancel() async {
    await _channel.invokeMethod('cancel', <String, dynamic>{'id': id});
  }
}

class EventOptions {
  int startTime;
  int duration;
  String viewName;
  Map<String, String> meta;
  String backendTracingID;
}

class BackendTracingIDParser {
  static final String headerKey = "server-timing";
  static final RegExp headerValueRegex = RegExp("^.* ?intid;desc=([^,]+)?.*\$");

  static String fromHeadersMap(Map<String, String> headers) {
    var result;
    headers.forEach((key, value) {
      if (key.toLowerCase() == headerKey.toLowerCase()) {
        result = headerValueRegex.firstMatch(value)?.group(1);
      }
    });
    return result;
  }
}
