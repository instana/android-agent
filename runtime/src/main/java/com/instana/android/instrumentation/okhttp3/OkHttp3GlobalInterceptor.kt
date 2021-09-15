/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.checkTag
import com.instana.android.core.util.ConstantsAndUtil.hasTrackingHeader
import com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled
import com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL
import com.instana.android.core.util.ConstantsAndUtil.isLibraryCallBoolean
import com.instana.android.core.util.Logger
import com.instana.android.instrumentation.HTTPMarker
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * This interceptor will be added automatically by plugin and OkHttpAspect
 * Also you can add this interceptor to your OkHttp client builder manually
 */
object OkHttp3GlobalInterceptor : Interceptor {

    private val httpMarkers = ConcurrentHashMap<String, HTTPMarker>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val intercepted = chain.request()
        val header = intercepted.header(TRACKING_HEADER_KEY)
        val url = intercepted.url().toString()

        val request: Request
        var marker: HTTPMarker? = null

        if (isAutoEnabled && !hasTrackingHeader(header) && !isBlacklistedURL(url)) {
            if (!checkTag(header) && !isLibraryCallBoolean(url)) {
                marker = Instana.startCapture(url)
                request = if (marker != null) {
                    Logger.d("Automatically marked OkHttp3 request with: `url` $url")
                    httpMarkers[marker.headerValue()] = marker
                    chain.request().newBuilder().header(TRACKING_HEADER_KEY, marker.headerValue()).build()
                } else {
                    Logger.e("Failed to automatically mark OkHttp3 request with: `url` $url")
                    intercepted
                }
            } else {
                Logger.d("Skipped already tagged OkHttp3 request with: `url` $url")
                request = intercepted
            }
        } else {
            Logger.d("Ignored OkHttp3 request with: `url` $url")
            request = intercepted
        }

        return try {
            val response = chain.proceed(request)
            Logger.d("Finishing OkHttp3 request with: `url` $url")
            marker?.run {
                finish(response)
                httpMarkers.remove(marker.headerValue(), marker)
            }
            response
        } catch (e: Exception) {
            Logger.d("Finishing OkHttp3 request with: `url` $url, `error` ${e.message}")
            marker?.run {
                finish(request, e)
                httpMarkers.remove(marker.headerValue(), marker)
            }
            throw e
        }
    }

    fun cancel(request: Request) {
        val cancelledTrackerValue: String? = request.header(TRACKING_HEADER_KEY)
        if (cancelledTrackerValue == null) {
            Logger.w("No marker found for cancelled OkHttp3 request with: 'url' ${request.url()}")
            return
        }

        var marker = httpMarkers[cancelledTrackerValue]
        if (marker == null) {
            @Suppress("UNNECESSARY_SAFE_CALL") // Crash reports suggest `request.url()` is indeed nullable
            marker = request.url()?.let { Instana.startCapture(it.toString()) }
        }
        marker?.run {
            cancel()
            httpMarkers.remove(this.headerValue())
        }
    }
}
