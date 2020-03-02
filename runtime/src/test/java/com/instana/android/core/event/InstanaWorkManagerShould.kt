package com.instana.android.core.event

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.instana.android.BaseTest
import com.instana.android.InstanaShould
import com.instana.android.core.InstanaConfiguration
import com.instana.android.core.InstanaWorkManager
import com.instana.android.crash.CrashEventStore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Assert.assertTrue
import org.junit.Test

class InstanaWorkManagerShould : BaseTest() {

    private val mockManager = mock<WorkManager>()

    private var manager: InstanaWorkManager

    init {
        CrashEventStore.init(app)
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        manager = InstanaWorkManager(InstanaConfiguration(InstanaShould.SERVER_URL, InstanaShould.API_KEY, eventsBufferSize = 2), mockManager)
    }

    @Test
    fun addEventToManager() {
        manager.send(EventFactory.createRemoteCall("name", "method", "type", 0L, 10L, 200))
        manager.send(EventFactory.createRemoteCall("name", "method", "type", 10L, 110L, 200))
        verify(mockManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }

    @Test
    fun addEventToManagerBelowBufferSize() {
        manager.send(EventFactory.createRemoteCall("url", "method", "error", 10L, 110L, 400))
        verifyNoMoreInteractions(mockWorkManager)
    }

    @Test
    fun saveAndSendCrash() {
        val instanaWorkManager = InstanaWorkManager(InstanaConfiguration(InstanaShould.SERVER_URL, InstanaShould.API_KEY, eventsBufferSize = 2), mockManager)
        val crash = EventFactory.createCrash("VERSION", "200", emptyList(), "stacktrace", hashMapOf())
        instanaWorkManager.persistCrash(crash)
        assertTrue(CrashEventStore.serialized.contains(crash.sessionId))
        assertTrue(CrashEventStore.serialized.contains(crash.id.toString()))
        assertTrue(CrashEventStore.serialized.contains("VERSION"))
        assertTrue(IdProvider.sessionId == crash.sessionId)

        InstanaWorkManager(InstanaConfiguration(InstanaShould.SERVER_URL, InstanaShould.API_KEY, eventsBufferSize = 2), mockManager)
        verify(mockManager).enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>())
    }
}