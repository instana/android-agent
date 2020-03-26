package com.instana.android

import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.core.InstanaConfig
import org.junit.Test

class InstanaShould : BaseTest() {

    init {
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
    }

    @Test
    fun init() {
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
        checkNotNull(Instana.customEvents)
        checkNotNull(Instana.instrumentationService)
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithFakeServerUrl() {
        Instana.setup(app, InstanaConfig(API_KEY, FAKE_SERVER_URL))
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithEmptyServerUrl() {
        Instana.setup(app, InstanaConfig(API_KEY, ""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun initWithEmptyApiKey() {
        Instana.setup(app, InstanaConfig("", SERVER_URL))
    }

    @Test
    fun getCrashReporting() {
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
        checkNotNull(Instana.crashReporting)
    }

    @Test
    fun getInstrumentationService() {
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
        checkNotNull(Instana.instrumentationService)
    }

    companion object {
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
        const val FAKE_SERVER_URL = "www.server_url.com"
    }
}