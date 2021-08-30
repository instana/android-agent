/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

import 'dart:async';

import 'package:flutter/services.dart';

/// Class providing all methods related to the setup and usage of the Instana Flutter agent
class InstanaAgent {
  static const MethodChannel _channel = const MethodChannel('instana_agent');

  /// Initializes Instana with a [key] and [reportingUrl] and optionally with SetupOptions
  ///
  /// Please run this as soon as possible in your app's lifecycle
  static Future<void> setup(
      {required String key, required String reportingUrl, SetupOptions? options}) async {
    return await _channel.invokeMethod('setup', <String, dynamic>{
                              'key': key,
                              'reportingUrl': reportingUrl,
                              'collectionEnabled': options?.collectionEnabled
                              });
  }

  /// Enable or disable collection (opt-in or opt-out)
  ///
  ///
  /// If needed, you can set collectionEnabled to false via Instana's setup and enable the collection later. (e.g. after giving the consent)
  /// Note: Any instrumentation is ignored when setting collectionEnabled to false.
  static Future<void> setCollectionEnabled(bool enable) async {
    await _channel.invokeMethod('setCollectionEnabled', <String, dynamic>{'collectionEnabled': enable});
  }

  /// Returns unique ID assigned by Instana to current session
  ///
  /// SessionID will change every time the app cold-starts
  static Future<String?> getSessionID() async {
    return await _channel.invokeMethod('getSessionID', <String, dynamic>{});
  }

  /// Sets custom User ID which all new beacons will be associated with
  ///
  /// Max length: 128 characters
  static Future<void> setUserID(String? userID) async {
    await _channel
        .invokeMethod('setUserID', <String, dynamic>{'userID': userID});
  }

  /// Sets User name which all new beacons will be associated with
  ///
  /// Max length: 128 characters
  static Future<void> setUserName(String? name) async {
    await _channel
        .invokeMethod('setUserName', <String, dynamic>{'userName': name});
  }

  /// Sets User email which all new beacons will be associated with
  ///
  /// Max length: 128 characters
  static Future<void> setUserEmail(String? email) async {
    await _channel
        .invokeMethod('setUserEmail', <String, dynamic>{'userEmail': email});
  }

  /// Sets Human-readable name of logical view to which new beacons will be associated
  ///
  /// Max length: 256 characters
  static Future<void> setView(String? name) async {
    await _channel.invokeMethod('setView', <String, dynamic>{'viewName': name});
  }

  /// Sets Key-Value pair which all new beacons will be associated with
  ///
  /// Max Key Length: 98 characters
  ///
  /// Max Value Length: 1024 characters
  static Future<void> setMeta(
      {required String key, required String value}) async {
    await _channel
        .invokeMethod('setMeta', <String, dynamic>{'key': key, 'value': value});
  }

  /// Sends a Custom Event beacon to Instana
  static Future<void> reportEvent(
      {required String name, EventOptions? options}) async {
    await _channel.invokeMethod('reportEvent', <String, dynamic>{
      'eventName': name,
      'startTime': options?.startTime?.toDouble(),
      'duration': options?.duration?.toDouble(),
      'viewName': options?.viewName,
      'meta': options?.meta,
      'backendTracingID': options?.backendTracingID
    });
  }

  /// Mark the start of an HTTP Request
  ///
  /// Returns a [Marker] you can [finish()] to send a beacon to Instana
  static Future<Marker> startCapture(
      {required String url, required String method, String? viewName}) async {
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

/// This class can be user to manually track HTTP Requests
///
/// Please use the [startCapture()] method to obtain your [Marker]
class Marker {
  Marker(
      {required MethodChannel channel,
      required this.id,
      required this.viewName})
      : _channel = channel;

  final MethodChannel _channel;
  final String? id;
  final String? viewName;

  /// Response's HTTP Status Code
  int? responseStatusCode;

  /// Backend Trace ID obtained from the [BackendTracingIDParser.headerKey] header of the response
  ///
  /// You can use the included [BackendTracingIDParser.fromHeadersMap(headers)] method to extract it from the response
  ///
  /// This will be used to correlate backend requests and tracking beacons
  String? backendTracingID;

  /// Response's Header-size in bytes
  int? responseSizeHeader;

  /// Response's compressed Body-size in bytes
  int? responseSizeBody;

  /// Response's uncompressed Body-size in bytes
  int? responseSizeBodyDecoded;

  /// Response's error message
  String? errorMessage;

  /// Finishes the [Marker], triggering the generation and queueing of a HTTP tracking beacon
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

  /// Cancels the [Marker], triggering the generation and queueing of a HTTP tracking beacon
  Future<void> cancel() async {
    await _channel.invokeMethod('cancel', <String, dynamic>{'id': id});
  }
}

class SetupOptions {
  ///  Enable or disable collection (instrumentation) on setup. Can be changed later via the property `collectionEnabled` (Default: true)
  bool collectionEnabled = true;
}

/// This class contains all the options you can provide for the Custom Events reported through [InstanaAgent.reportEvent()]
class EventOptions {
  /// Start Time in milliseconds since epoch
  ///
  /// If not set, it will default to the time of creation of the beacon
  int? startTime;

  /// Duration in milliseconds
  ///
  /// If not set, it will default to 0
  int? duration;

  /// View name
  ///
  /// If not set, it will default to the View name set in [InstanaAgent.setView()]
  String? viewName;

  /// Maps of Key-Value pairs which this Custom Event will be associated with. This will not affect any other beacons
  ///
  /// Max Key Length: 98 characters
  ///
  /// Max Value Length: 1024 characters
  Map<String, String>? meta;

  /// Backend Trace ID to associate this Custom Event to
  String? backendTracingID;
}

/// Helper class to make the manual extraction of the BackendTracingID easier
///
/// The BackendTracingID will be extracted from the [headerKey] header
class BackendTracingIDParser {
  static final String headerKey = "server-timing";
  static final RegExp headerValueRegex = RegExp("^.* ?intid;desc=([^,]+)?.*\$");

  /// Returns the BackendTracingID or null
  static String? fromHeadersMap(Map<String, String> headers) {
    var result;
    headers.forEach((key, value) {
      if (key.toLowerCase() == headerKey.toLowerCase()) {
        result = headerValueRegex.firstMatch(value)?.group(1);
      }
    });
    return result;
  }
}
