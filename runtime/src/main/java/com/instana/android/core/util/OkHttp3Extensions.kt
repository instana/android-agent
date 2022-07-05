/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import okhttp3.Headers
import okhttp3.Response
import okio.Buffer
import okio.GzipSource

fun Response.decodedContentLength(): Long? {
    val source = body()?.source() ?: return null

    source.request(Long.MAX_VALUE)
    var buffer = source.buffer()

    if ("gzip".equals(header("Content-Encoding"), ignoreCase = true)) {
        GzipSource(buffer.clone()).use { gzippedResponseBody ->
            buffer = Buffer()
            buffer.writeAll(gzippedResponseBody)
        }
    }

    return buffer.size()
}

fun Headers.toMap(): Map<String, String> {
    return this.toMultimap()
        .mapValues { entry -> entry.value.joinToString(",") }
}
