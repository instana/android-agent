/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.instrumentation.okhttp3

import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.core.util.toMap
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Request
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OkHttp3InstrumentationTest {

    @Mock
    lateinit var mockDispatcher: okhttp3.Dispatcher

    @Mock
    lateinit var mockCall: okhttp3.Call

    @Mock
    lateinit var mockBuilder: okhttp3.OkHttpClient.Builder

    @Before
    fun `test setup`() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `test check cancelAllCall triggers internal functions`() {
        OkHttp3Instrumentation.cancelAllCall(mockDispatcher)
        verify(mockDispatcher, atLeastOnce()).runningCalls()
        verify(mockDispatcher, atLeastOnce()).queuedCalls()
    }

    @Test
    fun `test check clientBuilderInterceptor triggers internal functions`() {
        OkHttp3Instrumentation.clientBuilderInterceptor(mockBuilder)
        verify(mockBuilder, atLeastOnce()).interceptors()
    }

    // ── clientBuilderInterceptor — idempotency ────────────────────────────────

    @Test
    fun `clientBuilderInterceptor adds interceptor to fresh builder`() {
        val builder = okhttp3.OkHttpClient.Builder()
        OkHttp3Instrumentation.clientBuilderInterceptor(builder)
        assert(builder.interceptors().count { it === OkHttp3GlobalInterceptor } == 1) {
            "Expected exactly 1 OkHttp3GlobalInterceptor, got ${builder.interceptors().size}"
        }
    }

    @Test
    fun `clientBuilderInterceptor does not add duplicate when interceptor already present`() {
        // Simulates the copy-constructor path: newBuilder() copies interceptors from parent,
        // then the instrumented <init> calls clientBuilderInterceptor again.
        val builder = okhttp3.OkHttpClient.Builder()
        builder.addInterceptor(OkHttp3GlobalInterceptor)  // already present (copied from parent)
        OkHttp3Instrumentation.clientBuilderInterceptor(builder)
        assert(builder.interceptors().count { it === OkHttp3GlobalInterceptor } == 1) {
            "Guard failed: interceptor duplicated. Count=${builder.interceptors().size}"
        }
    }

    // ── clientBuildSafetyCheck — safety-net deduplication ────────────────────

    @Test
    fun `clientBuildSafetyCheck is a no-op when interceptor is absent`() {
        val builder = okhttp3.OkHttpClient.Builder()
        OkHttp3Instrumentation.clientBuildSafetyCheck(builder)
        assert(builder.interceptors().isEmpty()) { "Expected empty interceptor list" }
    }

    @Test
    fun `clientBuildSafetyCheck is a no-op when interceptor appears exactly once`() {
        val builder = okhttp3.OkHttpClient.Builder()
        builder.addInterceptor(OkHttp3GlobalInterceptor)
        OkHttp3Instrumentation.clientBuildSafetyCheck(builder)
        assert(builder.interceptors().count { it === OkHttp3GlobalInterceptor } == 1) {
            "Safety check should not remove the single interceptor"
        }
    }

    @Test
    fun `clientBuildSafetyCheck removes duplicate OkHttp3GlobalInterceptor leaving exactly one`() {
        val builder = okhttp3.OkHttpClient.Builder()
        builder.addInterceptor(OkHttp3GlobalInterceptor)
        builder.addInterceptor(OkHttp3GlobalInterceptor) // duplicate — simulates the bug
        assert(builder.interceptors().count { it === OkHttp3GlobalInterceptor } == 2) { "Setup failed" }

        OkHttp3Instrumentation.clientBuildSafetyCheck(builder)

        assert(builder.interceptors().count { it === OkHttp3GlobalInterceptor } == 1) {
            "Safety check should reduce duplicates to 1, got ${builder.interceptors().size}"
        }
    }

    @Test
    fun `clientBuildSafetyCheck preserves non-Instana interceptors alongside single Instana interceptor`() {
        val otherInterceptor = okhttp3.Interceptor { chain -> chain.proceed(chain.request()) }
        val builder = okhttp3.OkHttpClient.Builder()
        builder.addInterceptor(otherInterceptor)
        builder.addInterceptor(OkHttp3GlobalInterceptor)
        builder.addInterceptor(OkHttp3GlobalInterceptor) // duplicate
        builder.addInterceptor(otherInterceptor)

        OkHttp3Instrumentation.clientBuildSafetyCheck(builder)

        val interceptors = builder.interceptors()
        assert(interceptors.count { it === OkHttp3GlobalInterceptor } == 1) {
            "Expected 1 Instana interceptor after safety check"
        }
        assert(interceptors.count { it === otherInterceptor } == 2) {
            "Safety check must not remove non-Instana interceptors"
        }
        assert(interceptors.size == 3) { "Expected 3 interceptors total, got ${interceptors.size}" }
    }

    @Test
    fun `test check if cancel call triggers request calls to cancel`() {
        try {
            OkHttp3Instrumentation.cancelCall(mockCall)
            verify(mockCall, atLeastOnce()).request()
        } catch (e: Exception) {
            verify(mockCall, atLeastOnce()).request()
        }
    }

    @Test
    fun `test check if cancel call triggers request calls to cancel woth mock caller`() {
        try {
            val mockRequest = mock(Request::class.java)
            val mockHeaders = mock(Headers::class.java)
            `when`(mockCall.request()).thenReturn(mockRequest)
            `when`(mockCall.request().url()).thenReturn(HttpUrl.get("http://www.google.com"))
            `when`(mockCall.request().header(ConstantsAndUtil.TRACKING_HEADER_KEY)).thenReturn("header")
            `when`(mockCall.request().headers()).thenReturn(mockHeaders)
            `when`(mockCall.request().headers().toMap()).thenReturn(emptyMap())
            OkHttp3Instrumentation.cancelCall(mockCall)
            verify(mockCall, atLeastOnce()).request()
        } catch (e: Exception) {
            verify(mockCall, atLeastOnce()).request()
        }
    }


}
