/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.instrumentation

import com.instana.android.BaseTest
import com.instana.android.InstanaShould.Companion.API_KEY
import com.instana.android.InstanaShould.Companion.FAKE_SERVER_URL
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertNotNull
import org.junit.Test

class InstrumentationServiceShould : BaseTest() {

    private val managerMock: InstanaWorkManager = mock()

    private val configuration = InstanaConfig(API_KEY, FAKE_SERVER_URL)
    private val instrumentationService = InstrumentationService(app, managerMock, configuration)

    @Test
    fun markCall() {
        val instrumentation = instrumentationService.markCall("Url", "POST")
        assertNotNull(instrumentation)
    }

    @Test
    fun markCallWithUrl() {
        val instrumentation = instrumentationService.markCall("Url", null)
        assertNotNull(instrumentation)
    }
}