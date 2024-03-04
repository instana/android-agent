/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android

import android.app.Application
import com.instana.android.core.InstanaConfig
import com.instana.android.session.SessionService
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
        resetConfig()
    }

    @Test
    fun `test report event will have details in the beacon`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val customEvent  = CustomEvent("TEST_EVENT").apply {
            customMetric = 212.223
            duration = 203
            meta = mapOf("tst" to "test")
            backendTracingID = "12345678"
            startTime = System.currentTimeMillis()
            error = Throwable("test")
            viewName = "view name"
        }
        Instana.reportEvent(customEvent)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("TEST_EVENT"))
        resetConfig()
    }

    @Test
    fun `test instrumentation service not available then return null from start capture`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.instrumentationService = null
        val httpMaker = Instana.startCapture("https://www.test.com")
        assert(httpMaker == null)
        resetConfig()
    }

    @Test
    fun `test instrumentation service available then should not return null from start capture`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        val httpMaker = Instana.startCapture("https://www.test.com")
        assert(httpMaker != null)
        resetConfig()
    }

    @Test
    fun `test set collection enabled should update instana object`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.setCollectionEnabled(true)
        assert(Instana.config?.collectionEnabled==true)
        assert(Instana.isCollectionEnabled()==true)
        resetConfig()
    }

    @Test
    fun `test user details is kept in object`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.userName = "User"
        Instana.userEmail = "email"
        Instana.userId = "Id"
        SessionService(app,Instana.workManager!!,config)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("ui\t${Instana.userId}"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("un\t${Instana.userName}"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("ue\t${Instana.userEmail}"))
        resetConfig()
    }


    @Test
    fun `test set googlePlayServicesMissing value should update device profile`(){
        val app: Application = app
        val config = InstanaConfig(API_KEY, SERVER_URL)
        Instana.setup(app, config)
        Instana.googlePlayServicesMissing = true
        assert(Instana.deviceProfile.googlePlayServicesMissing==true)
        resetConfig()
    }
    private fun resetConfig(){
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