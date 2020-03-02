package com.instana.android.instrumentation

import com.instana.android.BaseTest
import com.instana.android.InstanaShould.Companion.API_KEY
import com.instana.android.InstanaShould.Companion.FAKE_SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.BaseEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertNotNull
import org.junit.Test

class InstrumentationServiceShould : BaseTest() {

    private val managerMock: InstanaWorkManager = mock()

    private val configuration = InstanaConfig(FAKE_SERVER_URL, API_KEY)
    private val instrumentationService = InstrumentationService(app, managerMock, configuration)

    @Test
    fun markCall() {
        val instrumentation = instrumentationService.markCall("Url", "POST")
        assertNotNull(instrumentation)
    }

    @Test
    fun reportCallFull() {
        instrumentationService.reportCall("Url", "POST", 0L, 1L, 200)
        verify(managerMock).send(any<BaseEvent>())
    }

    @Test
    fun reportCallWithError() {
        instrumentationService.reportCall("Url", "POST", 0L, 1L, "error")
        verify(managerMock).send(any<BaseEvent>())
    }

    @Test
    fun markCallWithUrl() {
        val instrumentation = instrumentationService.markCall("Url")
        assertNotNull(instrumentation)
    }
}