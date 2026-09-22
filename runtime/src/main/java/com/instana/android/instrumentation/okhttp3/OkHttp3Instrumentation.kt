/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.core.util.Logger
import com.instana.android.core.util.instanaGenericExceptionFallbackHandler
import com.instana.android.instrumentation.okhttp3.OkHttp3Instrumentation.Companion.clientBuilderInterceptor


@Suppress("unused")
class OkHttp3Instrumentation {

    companion object {

        @JvmStatic
        fun clientBuilderInterceptor(builder: okhttp3.OkHttpClient.Builder) {
            try {
                Logger.i("OkHttp3: builder detected")
                if (builder.interceptors().contains(OkHttp3GlobalInterceptor).not()) {
                    builder.addInterceptor(OkHttp3GlobalInterceptor)
                    Logger.i("OkHttp3: added interceptor to builder")
                } else {
                    Logger.i("OkHttp3: interceptor was already present in builder")
                }
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "OkHttp3Instrumentation", at = "OkHttp3: clientBuilderInterceptor")
            }
        }

        /**
         * Safety-net deduplication called just before OkHttpClient.Builder.build() returns.
         *
         * The constructor-level guard in [clientBuilderInterceptor] normally prevents duplication,
         * but in edge cases (e.g. OkHttp version skew, reflective builder construction, or
         * any future code path that creates a Builder without going through <init>) a second
         * copy of [OkHttp3GlobalInterceptor] could still end up in the list. This method
         * removes all but the first occurrence before the immutable OkHttpClient is assembled,
         * making the guard two-layered and crash-proof.
         *
         * Called by plugin-instrumented bytecode at the exit of Builder.build().
         */
        @JvmStatic
        fun clientBuildSafetyCheck(builder: okhttp3.OkHttpClient.Builder) {
            try {
                val interceptors = builder.interceptors()
                val firstIndex = interceptors.indexOfFirst { it === OkHttp3GlobalInterceptor }
                if (firstIndex < 0) return // not present at all — nothing to deduplicate
                // Remove every occurrence after the first, iterating backwards to preserve indices
                for (i in interceptors.indices.reversed()) {
                    if (i != firstIndex && interceptors[i] === OkHttp3GlobalInterceptor) {
                        interceptors.removeAt(i)
                        Logger.w("OkHttp3: removed duplicate OkHttp3GlobalInterceptor at index $i (safety-net)")
                    }
                }
            } catch (e: Exception) {
                e.instanaGenericExceptionFallbackHandler(classType = "OkHttp3Instrumentation", at = "OkHttp3: clientBuildSafetyCheck")
            }
        }

        @JvmStatic
        fun cancelCall(call: okhttp3.Call) {
            try {
                Logger.i("OkHttp3: intercepted single-call cancel")
                OkHttp3GlobalInterceptor.cancel(call.request())
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "OkHttp3Instrumentation", at = "OkHttp3: cancelCall")
            }
        }

        @JvmStatic
        fun cancelAllCall(dispatcher: okhttp3.Dispatcher) {
            try {
                Logger.i("OkHttp3: intercepted dispatcher all-call cancel")
                for (call in dispatcher.runningCalls()) {
                    OkHttp3GlobalInterceptor.cancel(call.request())
                }
                for (call in dispatcher.queuedCalls()) {
                    OkHttp3GlobalInterceptor.cancel(call.request())
                }
            }catch (e:Exception){
                e.instanaGenericExceptionFallbackHandler(classType = "OkHttp3Instrumentation", at = "OkHttp3: cancelAllCall")
            }

        }

    }

}
