package com.instana.android.core.event

import com.instana.android.core.IdProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EventFactoryShould {

    @Test
    fun createRemoteCallEvent() {
        val event = EventFactory.createRemoteCall("url", "get", "success", System.currentTimeMillis(), 1L, 200)
        assertNotNull(event)
        assertEquals(IdProvider.sessionId, event.sessionId)
        assertEquals(1L, event.event.durationMs)
        assertEquals(200, event.event.remoteCall.responseCode)
    }

    @Test
    fun createRemoteCallEventError() {
        val event = EventFactory.createRemoteCall("url", "get", "success", System.currentTimeMillis(), 1L, "error")
        assertNotNull(event)
        assertEquals(IdProvider.sessionId, event.sessionId)
        assertEquals(1L, event.event.durationMs)
    }

    @Test
    fun createCustomEvent() {
        val event = EventFactory.createCustom(mapOf("this" to "that"), System.currentTimeMillis(), 1L)
        assertNotNull(event)
        assertEquals(IdProvider.sessionId, event.sessionId)
        assertEquals(1L, event.event.durationMs)
    }

    @Test
    fun createSessionEvent() {
        val event = EventFactory.createSession("7", Pair("10", "10"), "id", "google", "samsung", false)
        assertNotNull(event)
        assertEquals(IdProvider.sessionId, event.sessionId)
        assertEquals("10", event.profile.appVersion)
    }

    @Test
    fun createCrashEvent() {
        val event = EventFactory.createCrash("10", "10", listOf("onCreate", "onPause"), "trace", mapOf("this" to "that"))
        assertNotNull(event)
        assertEquals(IdProvider.sessionId, event.sessionId)
        assertEquals("trace", event.crash.report)
    }
}