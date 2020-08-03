package com.instana.android.instrumentation

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager


class InstrumentationService(
    private val context: Context,
    private val manager: InstanaWorkManager,
    private val config: InstanaConfig
) {

    private val tags = mutableSetOf<String>()

    val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun setType(type: HTTPCaptureConfig) {
        // change instrumentation type
        config.httpCaptureConfig = type
    }

    fun markCall(url: String, viewName: String?): HTTPMarker = HTTPMarker(url, viewName, context, manager, config)

    fun hasTag(header: String): Boolean = tags.contains(header)

    fun addTag(tag: String) {
        tags.add(tag)
    }

    fun removeTag(tag: String) {
        tags.remove(tag)
    }
}
