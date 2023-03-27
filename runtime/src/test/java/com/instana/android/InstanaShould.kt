/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android

import com.instana.android.core.InstanaConfig
import org.junit.Test

class InstanaShould : BaseTest() {

    @Test
    fun init() {
        Instana.setup(app, InstanaConfig(API_KEY, SERVER_URL))
        checkNotNull(Instana.customEvents)
        checkNotNull(Instana.instrumentationService)
    }

    @Test
    fun initWithEmptyServerUrl() {
        Instana.setup(app, InstanaConfig(API_KEY, ""))
    }

    @Test
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