/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event

import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.session.SessionService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CustomEventServiceTest : BaseTest() {

    lateinit var config: InstanaConfig

    lateinit var wrkManager:InstanaWorkManager

    @Mock
    lateinit var connectivityManager: ConnectivityManager

    @Mock
    lateinit var telephonyManager: TelephonyManager

    @Before
    fun `test setup`() {
        config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        wrkManager = InstanaWorkManager(config, app)
        MockitoAnnotations.openMocks(this)
        Instana.sessionId = null
        SessionService(app, mockWorkManager, config)
        val service = CustomEventService(app, wrkManager, config = config, cm = connectivityManager, tm = telephonyManager)
        service.submit(
            eventName = "EVENT_NAME",
            startTime = 1709531867061,
            duration = 200,
            meta = mapOf(
                "key1" to "value1",
                "key2" to "value2"
            ),
            viewName = "CUSTOM_EVENT_VIEW_NAME",
            backendTracingID = "12345678",
            error = Throwable("Throwable Message"),
            customMetric = 232.3234223
        )
    }

    @Test
    fun `test is session Id is null submit should return instead of reporting`(){
        Instana.sessionId = null
        val service = CustomEventService(app, wrkManager, config = config, cm = connectivityManager, tm = telephonyManager)
        service.submit(
            eventName = "TEST_EVENT_NAME",
            startTime = 1709531867061,
            duration = 200,
            meta = mapOf(
                "key1" to "value1",
                "key2" to "value2"
            ),
            viewName = "CUSTOM_EVENT_VIEW_NAME",
            backendTracingID = "12345678",
            error = Throwable("Throwable Message"),
            customMetric = 232.3234223
        )
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tTEST_EVENT_NAME") }.size
        assert(size==0)
    }

    @Test
    fun `test custom event beacon type is custom`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("t\tcustom") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has backend trace id provided`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("bt\t12345678") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has meta key and values`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("m_key1\tvalue1") && it.toString().contains("m_key2\tvalue2") }.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has view name`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("v\tCUSTOM_EVENT_VIEW_NAME")}.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has duration`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("d\t200")}.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has event name`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cen\tEVENT_NAME")}.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has start time`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("ti\t1709531867061")}.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has custom metric`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("cm\t232.3234223")}.size
        Assert.assertTrue(size > 0)
    }

    @Test
    fun `test custom event beacon has error message`() {
        wrkManager.isInitialDelayComplete = false
        val size = wrkManager.initialDelayQueue.filter { it.toString().contains("em\tThrowable Message")}.size
        Assert.assertTrue(size > 0)
    }
}