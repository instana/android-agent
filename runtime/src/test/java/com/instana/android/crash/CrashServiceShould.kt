package com.instana.android.crash

import com.instana.android.BaseTest
import com.instana.android.InstanaShould.Companion.API_KEY
import com.instana.android.InstanaShould.Companion.SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.event.models.legacy.CrashEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class CrashServiceShould : BaseTest() {

    private val configuration = InstanaConfig(SERVER_URL, API_KEY)
    private val mockHandler = mock<Thread.UncaughtExceptionHandler>()

    private val crashReporting = CrashService(app, mockWorkManager, configuration, mockHandler)

    @Test
    fun setEnabled() {
        crashReporting.enable()
        assertEquals(configuration.enableCrashReporting, true)
    }

    @Test
    fun setDisabled() {
        crashReporting.disable()
        assertEquals(configuration.enableCrashReporting, false)
    }

    @Test
    fun changeBufferSize() {
        crashReporting.changeBufferSize(20)
        assertEquals(configuration.breadcrumbsBufferSize, 20)
    }

    @Test
    fun leave() {
        crashReporting.leave("breadCrumb")
        val thread = Thread.currentThread()
        val throwable = Throwable()
        crashReporting.submitCrash(thread, throwable)

        argumentCaptor<CrashEvent>().apply {
            verify(mockWorkManager).persistCrash(capture())

            assertEquals(1, allValues.size)
            assertEquals("breadCrumb", firstValue.crash.breadCrumbs.first())
        }
    }

    @Test
    fun submitCrash() {
        val thread = Thread.currentThread()
        val throwable = Throwable()
        crashReporting.submitCrash(thread, throwable)
        verify(mockWorkManager).persistCrash(any())
    }
}