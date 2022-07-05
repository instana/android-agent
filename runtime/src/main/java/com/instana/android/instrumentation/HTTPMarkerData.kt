/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import androidx.annotation.IntRange
import androidx.annotation.Size

/**
 * Represents request/response properties related to a Marker
 *
 * @property requestMethod
 * HTTP request method as in https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods.
 *
 * For example: POST
 * @property responseStatusCode
 * HTTP response status code as in https://developer.mozilla.org/en-US/docs/Web/HTTP/Status.
 *
 * For example: 200
 *
 * @property responseSizeEncodedBytes Size of encoded/zipped response body, in bytes
 * @property responseSizeDecodedBytes Size of decoded/unzipped response body, in bytes
 * @property backendTraceId
 * Trace ID set by Instana-enabled servers and sent via the Server-Timing HTTP response header
 * This allows us to build a connection between end-user (mobile monitoring) and backend activity (tracing).
 *
 * For example: Server-Timing: intid;desc=bd777df70e5e5356
 * In this case the field should hold the value bd777df70e5e5356.
 * @property errorMessage
 * A freeform error message
 *
 * For example: "Error: Could not start a payment request"
 */
data class HTTPMarkerData(
    @param:Size(max = 16)
    val requestMethod: String? = null,
    @param:IntRange(from = 0)
    val responseStatusCode: Int? = null,
    @param:IntRange(from = 0)
    val responseSizeEncodedBytes: Long? = null,
    @param:IntRange(from = 0)
    val responseSizeDecodedBytes: Long? = null,
    @param:Size(max = 128)
    val backendTraceId: String? = null,
    @param:Size(max = 16384)
    val errorMessage: String? = null,
    @param:Size(max = 16384)
    val headers: Map<String, String>? = null
) {
    class Builder {
        var requestMethod: String? = null
            private set
        var responseStatusCode: Int? = null
            private set
        var responseSizeEncodedBytes: Long? = null
            private set
        var responseSizeDecodedBytes: Long? = null
            private set
        var backendTraceId: String? = null
            private set
        var errorMessage: String? = null
            private set
        var headers: Map<String, String>? = null
            private set

        /**
         * HTTP request method as in https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods
         *
         * For example: POST
         */
        fun requestMethod(@Size(max = 16) requestMethod: String?) = apply { this.requestMethod = requestMethod }

        /**
         * HTTP response status code as in https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
         *
         * For example: 200
         */
        fun responseStatusCode(@IntRange(from = 0) responseStatusCode: Int?) = apply { this.responseStatusCode = responseStatusCode }

        /**
         * Size of encoded/zipped response body, in bytes
         */
        fun responseSizeEncodedBytes(@IntRange(from = 0) responseSizeEncodedBytes: Long?) = apply { this.responseSizeEncodedBytes = responseSizeEncodedBytes }

        /**
         * Size of decoded/unzipped response body, in bytes
         */
        fun responseSizeDecodedBytes(@IntRange(from = 0) responseSizeDecodedBytes: Long?) = apply { this.responseSizeDecodedBytes = responseSizeDecodedBytes }

        /**
         * Trace ID set by Instana-enabled servers and sent via the Server-Timing HTTP response header
         * This allows us to build a connection between end-user (mobile monitoring) and backend activity (tracing).
         *
         * For example: Server-Timing: intid;desc=bd777df70e5e5356
         * In this case the field should hold the value bd777df70e5e5356.
         */
        fun backendTraceId(@Size(max = 128) backendTraceId: String?) = apply { this.backendTraceId = backendTraceId }

        /**
         * A freeform error message
         *
         * For example: "Error: Could not start a payment request"
         */
        fun errorMessage(@Size(max = 16384) errorMessage: String?) = apply { this.errorMessage = errorMessage }

        /**
         * A map of headers associated with the request/response
         */
        fun headers(@Size(max = 64) headers: Map<String, String>?) = apply { this.headers = headers }

        fun build() = HTTPMarkerData(
            requestMethod = requestMethod,
            responseStatusCode = responseStatusCode,
            responseSizeEncodedBytes = responseSizeEncodedBytes,
            responseSizeDecodedBytes = responseSizeDecodedBytes,
            backendTraceId = backendTraceId,
            errorMessage = errorMessage,
            headers = headers
        )
    }
}
