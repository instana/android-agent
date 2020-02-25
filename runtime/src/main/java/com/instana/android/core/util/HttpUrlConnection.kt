package com.instana.android.core.util

import android.os.Build
import java.net.HttpURLConnection

fun HttpURLConnection.isSuccessful(): Boolean =
    try {
        responseCode in 200..299
    } catch (ignored: Exception) {
        false
    }

fun HttpURLConnection.encodedResponseSizeOrNull(): Long? {
    var size: Long? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) contentLengthLong
        else contentLength.toLong()
    if (size == -1L) {
        size = null
    }
    return size
}

fun HttpURLConnection.responseCodeOrNull(): Int? =
    try {
        responseCode
    } catch (ignored: Exception) {
        null
    }

fun HttpURLConnection.errorMessageOrNull(): String? {
    return when {
        isSuccessful() -> null
        responseCodeOrNull() == null -> try {
            responseCode
            null
        } catch (e: Exception) {
            e.toString()
        }
        compareValues(encodedResponseSizeOrNull(), 0) > 0 -> errorStream?.use { it.readCopy() }
        else -> null
    }
}
