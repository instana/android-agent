/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.core.util.Logger


@Suppress("unused")
class OkHttp3Instrumentation {

    companion object {

        @JvmStatic
        fun clientBuilderInterceptor(builder: okhttp3.OkHttpClient.Builder) {
            Logger.i("OkHttp3: builder detected")
            if (builder.interceptors().contains(OkHttp3GlobalInterceptor).not()) {
                builder.addInterceptor(OkHttp3GlobalInterceptor)
                Logger.i("OkHttp3: added interceptor to builder")
            } else {
                Logger.i("OkHttp3: interceptor was already present in builder")
            }
        }

        @JvmStatic
        fun cancelCall(call: okhttp3.Call) {
            Logger.i("OkHttp3: intercepted single-call cancel")
            OkHttp3GlobalInterceptor.cancel(call.request())
        }

        @JvmStatic
        fun cancelAllCall(dispatcher: okhttp3.Dispatcher) {
            Logger.i("OkHttp3: intercepted dispatcher all-call cancel")
            for (call in dispatcher.runningCalls()) {
                OkHttp3GlobalInterceptor.cancel(call.request())
            }
            for (call in dispatcher.queuedCalls()) {
                OkHttp3GlobalInterceptor.cancel(call.request())
            }
        }

    }

}
