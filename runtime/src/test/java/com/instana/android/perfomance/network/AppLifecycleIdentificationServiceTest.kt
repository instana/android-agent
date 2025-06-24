/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance.network

import android.content.Intent
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.performance.network.AppLifecycleIdentificationService
import com.instana.android.performance.network.NetworkStatsHelper
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class AppLifecycleIdentificationServiceTest {

    private lateinit var service: AppLifecycleIdentificationService
    private lateinit var mockHelper: NetworkStatsHelper
    private lateinit var mockConfig: InstanaConfig

    @Before
    fun setup() {
        service = AppLifecycleIdentificationService()

        mockHelper = mock(NetworkStatsHelper::class.java)
        mockConfig = mock(InstanaConfig::class.java).apply {
            `when`(networkStatsHelper).thenReturn(mockHelper)
        }

        Instana.config = mockConfig
    }

    @Test
    fun `onTaskRemoved should trigger networkStatsHelper calculation`() {
        val intent = mock(Intent::class.java)

        service.onTaskRemoved(intent)

        verify(mockHelper).calculateNetworkUsage(false)
    }
}