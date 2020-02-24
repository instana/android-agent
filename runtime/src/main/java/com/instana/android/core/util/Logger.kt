package com.instana.android.core.util

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressLint("LogNotTimber")
object Logger {

    private const val LOG_TAG = "Instana"

    @Volatile
    var enabled = true

    @JvmStatic
    fun i(message: String) {
        if (enabled) {
            Log.i(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        if (enabled) {
            Log.w(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String, throwable: Throwable) {
        if (enabled) {
            Log.w(LOG_TAG, message, throwable)
        }
    }

    @JvmStatic
    fun e(message: String) {
        if (enabled) {
            Log.e(LOG_TAG, message)
        }
    }

    @JvmStatic
    fun e(message: String, s: String) {
        if (enabled) {
            Log.e(s, message)
        }
    }

    @JvmStatic
    fun d(s: String) {
        if (enabled) {
            Log.d(LOG_TAG, s)
        }
    }
}