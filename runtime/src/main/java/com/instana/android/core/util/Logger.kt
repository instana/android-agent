/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

import android.util.Log
import androidx.annotation.RestrictTo
import com.instana.android.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY)
object Logger {

    private const val LOG_TAG = "Instana"

    @Volatile
    var enabled = true

    var clientLogger: Logger? = null

    /**
     * android.util.Log levels
     */
    var logLevel = Log.INFO

    @JvmStatic
    fun v(message: String) {
        if (enabled && logLevel <= Log.VERBOSE) {
            clientLogger?.log(Log.VERBOSE, LOG_TAG, message, null) ?: Log.v(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun d(message: String) {
        if (enabled && logLevel <= Log.DEBUG) {
            clientLogger?.log(Log.DEBUG, LOG_TAG, message, null) ?: Log.d(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun i(message: String) {
        if (enabled && logLevel <= Log.INFO) {
            clientLogger?.log(Log.INFO, LOG_TAG, message, null) ?: Log.i(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        if (enabled && logLevel <= Log.WARN) {
            clientLogger?.log(Log.WARN, LOG_TAG, message, null) ?: Log.w(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String, throwable: Throwable) {
        if (enabled && logLevel <= Log.WARN) {
            clientLogger?.log(Log.WARN, LOG_TAG, message, throwable) ?: Log.w(LOG_TAG, message, throwable)
        }
    }

    @JvmStatic
    fun e(message: String) {
        if (enabled && logLevel <= Log.ERROR) {
            clientLogger?.log(Log.ERROR, LOG_TAG, message, null) ?: Log.e(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun e(message: String, throwable: Throwable) {
        if (enabled && logLevel <= Log.ERROR) {
            clientLogger?.log(Log.ERROR, LOG_TAG, message, throwable) ?: Log.e(LOG_TAG, message, throwable)
        }
    }
}
