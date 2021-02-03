/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

import 'package:http/http.dart';
import 'package:instana_agent/instana_agent.dart';

class InstrumentedHttpClient extends BaseClient {
  InstrumentedHttpClient(this._inner);

  final Client _inner;

  @override
  Future<StreamedResponse> send(BaseRequest request) async {
    final Marker marker = await InstanaAgent.startCapture(
        url: request.url.toString(), method: request.method);

    StreamedResponse response;
    try {
      response = await _inner.send(request);
      marker
        ..responseStatusCode = response.statusCode
        ..responseSizeBody = response.contentLength
        ..backendTracingID =
            BackendTracingIDParser.fromHeadersMap(response.headers);
    } finally {
      await marker.finish();
    }

    return response;
  }
}
