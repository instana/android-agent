package com.instana.android.instrumentation.okhttp

import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.util.ConstantsAndUtil.TRACKING_HEADER_KEY
import com.instana.android.instrumentation.HTTPCaptureConfig
import com.instana.android.instrumentation.okhttp3.OkHttp3GlobalInterceptor
import junit.framework.Assert.assertEquals
import okhttp3.OkHttpClient
import okhttp3.Request
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Test

class OkHttp3GlobalInterceptorShould : BaseTest() {

    private val interceptor = OkHttp3GlobalInterceptor.INSTANCE

    init {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }

    @Before
    fun setUp() {
        Instana.setup(app, InstanaConfiguration("http://10.0.2.2:3000/v1/api", "42"))
    }

    @Test
    fun interceptCallWithSuccess() {
        val client = OkHttpClient().newBuilder().addInterceptor(interceptor).build()
        val request = Request.Builder().url("https://www.google.com").get().build()
        val response = client.newCall(request).execute()
        assertThat(response.request().header(TRACKING_HEADER_KEY), IsInstanceOf(String::class.java))
    }

    @Test
    fun notInterceptCallDisabled() {
        Instana.remoteCallInstrumentation?.setType(HTTPCaptureConfig.NONE)
        val client = OkHttpClient().newBuilder().addInterceptor(interceptor).build()
        val request = Request.Builder().url("https://www.google.com").get().build()
        val response = client.newCall(request).execute()
        assertEquals(response.request().header(TRACKING_HEADER_KEY), null)
    }

    @Test
    fun notInterceptCallManual() {
        Instana.remoteCallInstrumentation?.setType(HTTPCaptureConfig.MANUAL)
        val client = OkHttpClient().newBuilder().addInterceptor(interceptor).build()
        val request = Request.Builder().url("https://www.google.com").get().build()
        val response = client.newCall(request).execute()
        assertEquals(response.request().header(TRACKING_HEADER_KEY), null)
    }
}