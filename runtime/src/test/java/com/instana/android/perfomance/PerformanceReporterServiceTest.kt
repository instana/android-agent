/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.performance.PerformanceMetric
import com.instana.android.performance.PerformanceReporterService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PerformanceReporterServiceTest:BaseTest() {


    private lateinit var performanceReporterService: PerformanceReporterService

    @Before
    fun setup(){
        performanceReporterService = PerformanceReporterService(app,mockWorkManager,config = InstanaConfig(
            InstanaTest.API_KEY,
            InstanaTest.SERVER_URL, enableCrashReporting = true)
        )
    }
    @Test
    fun `test sendPerformance function should send a performance beacon`(){
        Instana.sessionId = "1223"
        performanceReporterService.sendPerformance(
            PerformanceMetric.AppStartTime(12L,12L,33L)
        )
        Mockito.verify(mockWorkManager).queue(any())
    }

    @Test
    fun `test sendPerformance function should not send a performance beacon if session is null`(){
        Instana.sessionId = null
        performanceReporterService.sendPerformance(
            PerformanceMetric.AppNotResponding(12L,"","")
        )
        Mockito.verify(mockWorkManager, never()).queue(any())
    }

}