/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.util.Log
import com.instana.android.BaseTest
import com.instana.android.Logger
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoggerTest:BaseTest() {

    @Mock
    lateinit var logger:Logger

    @Mock
    lateinit var disabledLogger:Logger

    @Before
    fun `test setup`(){
        MockitoAnnotations.initMocks(this)
        com.instana.android.core.util.Logger.clientLogger = logger
        com.instana.android.core.util.Logger.enabled = true
    }

    @Test
    fun `test logging calls for v-VERBOSE if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.v("something")
        verify(logger, atLeastOnce()).log(Log.VERBOSE, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for v-VERBOSE if enabled and logLevel high`(){
        com.instana.android.core.util.Logger.logLevel = 3
        com.instana.android.core.util.Logger.v("something")
        verify(logger, never()).log(Log.VERBOSE, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for v-VERBOSE if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.v("something")
        verify(disabledLogger, never()).log(Log.VERBOSE, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for d-DEBUG if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.d("something")
        verify(logger, atLeastOnce()).log(Log.DEBUG, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for d-DEBUG if enabled and logLevel high`(){
        com.instana.android.core.util.Logger.logLevel = 4
        com.instana.android.core.util.Logger.d("something")
        verify(logger, never()).log(Log.DEBUG, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for d-DEBUG if enabled with lesser varient`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.d("something")
        verify(logger, atLeastOnce()).log(Log.DEBUG, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for d-DEBUG if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.d("something")
        verify(disabledLogger, never()).log(Log.DEBUG, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-Info if disabled`(){
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.i("something")
        verify(disabledLogger, never()).log(Log.INFO, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-Info if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.i("something")
        verify(logger, atLeastOnce()).log(Log.INFO, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-Info if enabled and logLevel high`(){
        com.instana.android.core.util.Logger.logLevel = 5
        com.instana.android.core.util.Logger.i("something")
        verify(logger, never()).log(Log.INFO, "Instana", "something", null)
    }


    @Test
    fun `test logging calls for i-WARN if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.w("something")
        verify(logger, atLeastOnce()).log(Log.WARN, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-WARN if enabled and greater loglevel`(){
        com.instana.android.core.util.Logger.logLevel = 6
        com.instana.android.core.util.Logger.w("something")
        verify(logger, never()).log(Log.WARN, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-WARN if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.w("something")
        verify(disabledLogger, never()).log(Log.WARN, "Instana", "something", null)
    }

    @Test
    fun `test logging calls for i-WARN-Throwable if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        val throwable = Throwable("test-throw")
        com.instana.android.core.util.Logger.w("something",throwable)
        verify(logger, atLeastOnce()).log(Log.WARN, "Instana", "something",throwable)
    }

    @Test
    fun `test logging calls for i-WARN-Throwable if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        val throwable = Throwable("test-throw")
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.w("something",throwable)
        verify(disabledLogger, never()).log(Log.WARN, "Instana", "something",throwable)
    }

    @Test
    fun `test logging calls for e-ERROR-Throwable if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        val throwable = Throwable("test-throw")
        com.instana.android.core.util.Logger.e("something",throwable)
        verify(logger, atLeastOnce()).log(Log.ERROR, "Instana", "something",throwable)
    }

    @Test
    fun `test logging calls for e-ERROR-Throwable if enabled with greater value of loglevel`(){
        com.instana.android.core.util.Logger.logLevel = 7
        val throwable = Throwable("test-throw")
        com.instana.android.core.util.Logger.e("something",throwable)
        verify(logger, never()).log(Log.ERROR, "Instana", "something",throwable)
    }

    @Test
    fun `test logging calls for e-ERROR-Throwable if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        val throwable = Throwable("test-throw")
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.e("something",throwable)
        verify(disabledLogger, never()).log(Log.ERROR, "Instana", "something",throwable)
    }

    @Test
    fun `test logging calls for e-ERROR if enabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.e("something")
        verify(logger, atLeastOnce()).log(Log.ERROR, "Instana", "something",null)
    }

    @Test
    fun `test logging calls for e-ERROR if disabled`(){
        com.instana.android.core.util.Logger.logLevel = 1
        com.instana.android.core.util.Logger.clientLogger = disabledLogger
        com.instana.android.core.util.Logger.enabled = false
        com.instana.android.core.util.Logger.e("something")
        verify(disabledLogger, never()).log(Log.ERROR, "Instana", "something",null)
    }


}