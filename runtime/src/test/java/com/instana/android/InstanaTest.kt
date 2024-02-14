/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android

import android.app.Application
import com.instana.android.core.InstanaConfig
import org.junit.Assert
import org.junit.Test

class InstanaTest : BaseTest() {

    @Test
    fun `test setup withInvalidConfig shouldNotInitialize Instana`() {
        // Given
        val app: Application = app
        val invalidConfig = InstanaConfig("", "")

        // When
        Instana.setup(app, invalidConfig)

        // Then
        Assert.assertTrue(Instana.config == null)
        Assert.assertNull(Instana.workManager)
        Assert.assertNull(Instana.instrumentationService)
        Assert.assertNull(Instana.customEvents)
        Assert.assertNull(Instana.crashReporting)
    }

    @Test
    fun `test setup withInvalidKey shouldNotInitialize Instana`() {
        // Given
        val app: Application = app
        val invalidConfig = InstanaConfig("", SERVER_URL)

        // When
        Instana.setup(app, invalidConfig)

        // Then
        Assert.assertTrue(Instana.config == null)
        Assert.assertNull(Instana.workManager)
        Assert.assertNull(Instana.instrumentationService)
        Assert.assertNull(Instana.customEvents)
        Assert.assertNull(Instana.crashReporting)
    }

    @Test
    fun `test setup withValidConfig shouldInitialize Instana`() {
        // Given
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)

        // When
        Instana.setup(app, config)

        // Then
        Assert.assertNotNull(Instana.config)
        Assert.assertNotNull(Instana.workManager)
        Assert.assertNotNull(Instana.instrumentationService)
        Assert.assertNotNull(Instana.customEvents)
        Assert.assertNotNull(Instana.crashReporting)
        Instana.config = null
        Instana.workManager = null
        Instana.instrumentationService = null
        Instana.customEvents = null
        Instana.crashReporting = null
    }

    companion object {
        const val API_KEY = "QPOEWIRJQPOIEWJF=-098767ALDJIFJASP"
        const val SERVER_URL = "https://www.google.com"
        const val FAKE_SERVER_URL = "www.server_url.com"
    }
}