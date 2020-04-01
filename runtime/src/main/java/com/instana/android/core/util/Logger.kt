package com.instana.android.core.util

import android.util.Log
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
object Logger {

    private const val LOG_TAG = "Instana"

    @Volatile
    var enabled = true

    /**
     * android.util.Log levels
     */
    var logLevel = Log.INFO

    @JvmStatic
    fun v(message: String) {
        if (enabled && logLevel <= Log.VERBOSE) {
            Log.v(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun d(message: String) {
        if (enabled && logLevel <= Log.DEBUG) {
            Log.d(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun i(message: String) {
        if (enabled && logLevel <= Log.INFO) {
            Log.i(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        if (enabled && logLevel <= Log.WARN) {
            Log.w(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String, throwable: Throwable) {
        if (enabled && logLevel <= Log.WARN) {
            Log.w(LOG_TAG, message, throwable)
        }
    }

    @JvmStatic
    fun e(message: String) {
        if (enabled && logLevel <= Log.ERROR) {
            Log.e(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun e(message: String, throwable: Throwable) {
        if (enabled && logLevel <= Log.ERROR) {
            Log.e(LOG_TAG, message, throwable)
        }
    }
}
