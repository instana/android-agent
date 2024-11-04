/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.os.Build
import java.io.IOException
import java.net.HttpURLConnection
import java.util.zip.GZIPInputStream

fun HttpURLConnection.isSuccessful(): Boolean =
    try {
        responseCode in 200..299
    } catch (ignored: Exception) {
        false
    }

fun HttpURLConnection.encodedResponseSizeOrNull(): Long? {
    return try {
        var size: Long? =
            if (Build.VERSION.SDK_INT >= 24) contentLengthLong
            else contentLength.toLong()
        if (size == -1L) {
            size = null
        }
        size
    }catch (e:Exception){
        e.instanaGenericExceptionFallbackHandler(type = "Extension", at = "encodedResponseSizeOrNull - contentLength")
        null
    }

}

fun HttpURLConnection.decodedResponseSizeOrNull(): Int? {
    try {
        if ("gzip".equals(contentEncoding, ignoreCase = true)) {
            try {
                GZIPInputStream(inputStream.clone()).use { return it.readBytes().size }
            } catch (e: IOException) {
                e.instanaGenericExceptionFallbackHandler(type = "IOException", at = "decodedResponseSizeOrNull")
                return null
            }
        }
    }catch (e:Exception){
        e.instanaGenericExceptionFallbackHandler(type = "Extension", at = "decodedResponseSizeOrNull")
    }
    return null
}

fun HttpURLConnection.responseCodeOrNull(): Int? =
    try {
        responseCode
    } catch (ignored: Exception) {
        null
    }

fun HttpURLConnection.errorMessageOrNull(): String? {
    return try {
        when {
            isSuccessful() -> null
            responseCodeOrNull() == null -> try {
                responseCode
                null
            } catch (e: Exception) {
                "responseCode - $e"
            }

            compareValues(encodedResponseSizeOrNull(), 0) > 0 -> try {
                errorStream?.use { it.readCopy() }
            } catch (e: Exception) {
                "errorStream - $e"
            }

            else -> null
        }
    }catch (e:Exception){
        e.instanaGenericExceptionFallbackHandler(type = "Extension", at = "errorMessageOrNull")
        return null
    }
}
