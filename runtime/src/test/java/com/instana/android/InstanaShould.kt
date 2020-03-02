package com.instana.android

import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.core.InstanaConfiguration
import org.junit.Test

class InstanaShould : BaseTest() {

    init {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }

    @Test
    fun init() {
        Instana.setup(app, InstanaConfiguration(SERVER_URL, API_KEY))
        checkNotNull(Instana.alert)
        checkNotNull(Instana.crashReporting)
        checkNotNull(Instana.remoteCallInstrumentation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithFakeServerUrl() {
        Instana.setup(app, InstanaConfiguration(FAKE_SERVER_URL, API_KEY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithEmptyServerUrl() {
        Instana.setup(app, InstanaConfiguration("", API_KEY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithEmptyApiKey() {
        Instana.setup(app, InstanaConfiguration(SERVER_URL, ""))
    }

    @Test
    fun getCrashReporting() {
        Instana.setup(app, InstanaConfiguration(SERVER_URL, API_KEY))
        checkNotNull(Instana.crashReporting)
    }

    @Test
    fun getAlert() {
        Instana.setup(app, InstanaConfiguration(SERVER_URL, API_KEY))
        checkNotNull(Instana.alert)
    }

    @Test
    fun getRemoteCallInstrumentation() {
        Instana.setup(app, InstanaConfiguration(SERVER_URL, API_KEY))
        checkNotNull(Instana.remoteCallInstrumentation)
    }

    companion object {
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
        const val FAKE_SERVER_URL = "www.server_url.com"
    }
}