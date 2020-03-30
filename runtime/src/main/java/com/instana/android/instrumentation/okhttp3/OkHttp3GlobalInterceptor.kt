package com.instana.android.instrumentation.okhttp3

import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.checkTag
import com.instana.android.core.util.ConstantsAndUtil.hasTrackingHeader
import com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled
import com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL
import com.instana.android.core.util.ConstantsAndUtil.isNotLibraryCallBoolean
import com.instana.android.core.util.Logger
import com.instana.android.instrumentation.HTTPMarker
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * This interceptor will be added automatically by plugin and OkHttpAspect
 * Also you can add this interceptor to your OkHttp client builder manually
 */
object OkHttp3GlobalInterceptor : Interceptor {

    private val httpMarkers = mutableListOf<HTTPMarker>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val intercepted = chain.request()
        val header = intercepted.header(TRACKING_HEADER_KEY)
        val url = intercepted.url.toString()

        val request: Request
        var marker: HTTPMarker? = null

        if (isAutoEnabled && !hasTrackingHeader(header) && !isBlacklistedURL(url)) {
            if (!checkTag(header) && isNotLibraryCallBoolean(url)) {
                marker = Instana.startCapture(url)
                request = if (marker != null) {
                    Logger.d("Automatically marked OkHttp3 request with: `url` $url")
                    httpMarkers.add(marker)
                    chain.request().newBuilder().header(marker.headerKey(), marker.headerValue()).build()
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
            marker?.finish(response)
            httpMarkers.remove(marker)
            response
        } catch (e: Exception) {
            Logger.d("Finishing OkHttp3 request with: `url` $url, `error` ${e.message}")
            marker?.finish(request, e)
            httpMarkers.remove(marker)
            chain.proceed(chain.request())
        }
    }

    fun cancel(request: Request) {
        val marker = httpMarkers.firstOrNull { it.headerValue() == request.header(it.headerKey()) }
            ?: Instana.startCapture(request.url.toString())
        marker?.cancel()
        httpMarkers.remove(marker)
    }
}