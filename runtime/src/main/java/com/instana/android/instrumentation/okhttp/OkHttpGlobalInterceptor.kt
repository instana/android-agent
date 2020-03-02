package com.instana.android.instrumentation.okhttp

import com.instana.android.Instana
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.core.util.ConstantsAndUtil.checkTag
import com.instana.android.core.util.ConstantsAndUtil.hasTrackingHeader
import com.instana.android.core.util.ConstantsAndUtil.isAutoEnabled
import com.instana.android.core.util.ConstantsAndUtil.isBlacklistedURL
import com.instana.android.core.util.ConstantsAndUtil.isNotLibraryCallBoolean
import com.instana.android.instrumentation.RemoteCallMarker
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * This interceptor will be added automatically by plugin and OkHttpAspect
 * Also you can add this interceptor to your OkHttp client builder manually
 */
class OkHttpGlobalInterceptor private constructor() : Interceptor {

    companion object {
        @JvmField
        val INSTANCE = OkHttpGlobalInterceptor()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val intercepted = chain.request()
        val header = intercepted.header(TRACKING_HEADER_KEY)
        val url = intercepted.url().toString()

        val request: Request
        var marker: RemoteCallMarker? = null

        if (isAutoEnabled && !hasTrackingHeader(header) && !isBlacklistedURL(url)) {
            if (!checkTag(header) && isNotLibraryCallBoolean(url)) {
                marker = Instana.remoteCallInstrumentation?.markCall(url, intercepted.method())!!
                request = chain.request().newBuilder().header(marker.headerKey(), marker.headerValue()).build()
            } else {
                request = intercepted
            }
        } else {
            request = intercepted
        }

        return try {
            val response = chain.proceed(request)
            marker?.endedWith(response)
            response
        } catch (e: Exception) {
            marker?.endedWith(request, e)
            chain.proceed(chain.request())
        }
    }
}