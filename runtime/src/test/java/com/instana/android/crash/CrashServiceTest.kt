/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.crash

import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.instana.android.BaseTest
import com.instana.android.InstanaTest.Companion.API_KEY
import com.instana.android.InstanaTest.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.nhaarman.mockitokotlin2.any
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class CrashServiceTest:BaseTest() {
    private lateinit var crashService: CrashService

    @Mock
    lateinit var connectivityManager:ConnectivityManager

    @Mock
    lateinit var telephonyManager: TelephonyManager
    @Before
    fun `setup crash Service`(){
        MockitoAnnotations.openMocks(this)
        crashService = CrashService(app = app, manager = mockWorkManager, config = InstanaConfig(API_KEY,SERVER_URL), cm = connectivityManager, tm = telephonyManager)
    }

    @Test
    fun `test for crash submit`(){
        val realThread = Thread() // create a real Thread instance
        val realThrowable = RuntimeException("Test Exception") // create a real Throwable instance

        crashService.submitCrash(realThread, realThrowable)

        verify(mockWorkManager).queueAndFlushBlocking(any())
    }
}