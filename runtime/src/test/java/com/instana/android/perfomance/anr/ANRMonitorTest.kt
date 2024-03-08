/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.perfomance.anr

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.performance.PerformanceMonitorConfig
import com.instana.android.performance.anr.ANRMonitor
import com.instana.android.performance.anr.AnrException
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ANRMonitorTest:BaseTest() {

    private lateinit var config: InstanaConfig

    private lateinit var wrkManager: InstanaWorkManager

    @Mock
    lateinit var anrThread:AnrException

    @Before
    fun `test setup`(){
        config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        Instana.workManager = null
        MockitoAnnotations.openMocks(this)
        Instana.setup(app,config)
        wrkManager = Instana.workManager!!
        //SessionService(app, mockWorkManager, config)
    }

    @Test
    fun `test stackTraceAsString is called on anrThread`(){
        mockInstanaLifeCycle.activityName = "MAIN_ACTIVITY"
        val anrMonitorReal = ANRMonitor(PerformanceMonitorConfig(),mockInstanaLifeCycle)
        wrkManager.isInitialDelayComplete = false
        anrMonitorReal.onAppNotResponding(anrThread,200)
        Thread.sleep(1000)
        assert(Instana.workManager?.initialDelayQueue.toString().contains("m_activityName\tactivity"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("m_stackTrace"))
        assert(Instana.workManager?.initialDelayQueue.toString().contains("cen\tANR"))
    }
}