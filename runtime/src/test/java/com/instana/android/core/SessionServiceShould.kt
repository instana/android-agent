package com.instana.android.core

import com.instana.android.BaseTest
import com.instana.android.core.event.models.SessionEvent
import com.instana.android.session.SessionService
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionServiceShould : BaseTest() {

    @Test
    fun sendProfileOnInstantiation() {
        SessionService(app, mockWorkManager)

        argumentCaptor<SessionEvent>().apply {
            verify(mockWorkManager).send(capture())

            assertEquals(1, allValues.size)
            assertEquals(IdProvider.sessionId, firstValue.sessionId)
            assertEquals("android", firstValue.profile.platform)
        }
    }
}