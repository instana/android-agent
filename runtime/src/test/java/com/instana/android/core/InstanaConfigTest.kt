/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core

import com.instana.android.core.event.models.Platform
import com.instana.android.instrumentation.HTTPCaptureConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstanaConfigTest {

    @Test
    fun `test isValid withBlankApiKey shouldReturnFalse`() {
        // Given
        val config = InstanaConfig("", "https://example.com")

        // When
        val isValid = config.isValid()

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test isValid withInvalidReportingUrl shouldReturnFalse`() {
        // Given
        val config = InstanaConfig("validApiKey", "invalidUrl")

        // When
        val isValid = config.isValid()

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test isValid withBlankReportingUrl shouldReturnFalse`() {
        // Given
        val config = InstanaConfig("validApiKey", "")

        // When
        val isValid = config.isValid()

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test isValid withInvalidUrl shouldReturnFalse`() {
        // Given
        val config = InstanaConfig("validApiKey", "notAValidUrl")

        // When
        val isValid = config.isValid()

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `test defaultValues shouldBeSetCorrectly`() {
        // Given
        val config = InstanaConfig("apiKey", "https://example.com")

        // Then
        assertEquals(HTTPCaptureConfig.AUTO, config.httpCaptureConfig)
        assertEquals(SuspendReportingType.LOW_BATTERY, config.suspendReporting)
        assertEquals(3000, config.initialBeaconDelayMs)
        assertTrue(config.collectionEnabled)
        assertFalse(config.enableCrashReporting)
        assertNull(config.slowSendIntervalMillis)
        assertEquals(-1L, config.usiRefreshTimeIntervalInHrs)
        assertEquals(1500, config.initialSetupTimeoutMs)
        assertFalse(config.debugTrustInsecureReportingURL)
        assertEquals(20, config.breadcrumbsBufferSize)
        assertEquals(Platform.ANDROID.internalType, config.hybridAgentId)
        assertEquals("", config.hybridAgentVersion)
    }

    @Test
    fun `test HybridAgentOptions params`(){
        val lengthMoreThan16:String = "12345678901234567890";
        val hybridAgentOptions = HybridAgentOptions(lengthMoreThan16,lengthMoreThan16)
        assert(hybridAgentOptions.id.length<lengthMoreThan16.length)
        assert(hybridAgentOptions.version.length < lengthMoreThan16.length)
    }

}
