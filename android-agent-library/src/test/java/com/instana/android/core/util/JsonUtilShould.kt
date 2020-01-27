package com.instana.android.core.util

import com.instana.android.BaseTest
import com.instana.android.core.IdProvider
import com.instana.android.core.event.EventFactory
import com.instana.android.core.event.models.SessionEvent
import com.instana.android.core.event.models.SessionPayloadEvent
import org.junit.Assert.*
import org.junit.Test

class JsonUtilShould : BaseTest() {

    @Test
    fun checkIfAdaptersAreNull() {
        assertNotNull(JsonUtil.EVENT_JSON_ADAPTER)
    }

    @Test
    fun doCrashEventParsingToJson() {
        val baseCrashEvent = EventFactory.createCrash("VERSION", "200", listOf("breadCrumb1", "breadCrumb2"), "stacktrace", hashMapOf("thread" to "threadStackTrace"))
        val json = JsonUtil.EVENT_JSON_ADAPTER.toJson(listOf(baseCrashEvent))
        assertNotNull(json)
        assertTrue(json.contains(IdProvider.sessionId))
        assertTrue(json.contains(baseCrashEvent.crash.timestamp.toString()))
    }

    @Test
    fun doRemoteEventParsingToJson() {
        val remoteCallEvent = EventFactory.createRemoteCall("url", "post", "error", 0L, 0L, 401)
        val json = JsonUtil.EVENT_JSON_ADAPTER.toJson(listOf(remoteCallEvent))
        assertNotNull(json)
        assertTrue(json.contains(IdProvider.sessionId))
        assertTrue(json.contains(remoteCallEvent.event.timestamp.toString()))
    }

    @Test
    fun doEventsParsingToJson() {
        val crash = EventFactory.createCrash("VERSION", "200", listOf("breadCrumb1", "breadCrumb2"), "stacktrace", hashMapOf("thread" to "threadStackTrace"))
        val remote = EventFactory.createRemoteCall("url", "post", "success", 0L, 0L, 200)
        val anr = EventFactory.createAnrAlert("activity", 0L)
        val lowMem = EventFactory.createLowMemAlert("activity", 10.toString(), 10.toString())
        val dip = EventFactory.createFrameDipAlert("activity", 0L, 1L)
        val session = SessionEvent(SessionPayloadEvent().apply {
            platform = "android"
            osLevel = "nougat"
            appBuild = "342"
            appVersion = "4.5"
        })
        val customEvent = EventFactory.createCustom(mapOf("this" to "that"), System.currentTimeMillis(), 1L)
        val json = JsonUtil.EVENT_JSON_ADAPTER.toJson(listOf(session, crash, remote, customEvent, anr, lowMem, dip))
        assertNotNull(json)
        assertTrue(json.contains(IdProvider.sessionId))
        assertTrue(json.contains("profile"))
        assertTrue(json.contains("crash"))
        assertTrue(json.contains("remoteCall"))
        assertTrue(json.contains("customEvent"))
        assertTrue(json.contains("anr"))
        assertTrue(json.contains("lowMemory"))
        assertTrue(json.contains("framerateDip"))
        assertTrue(json.contains(remote.event.timestamp.toString()))
    }

    @Test
    fun doSameEventsParsingToJson() {
        val baseRemoteEvent = EventFactory.createRemoteCall("url", "post", "success", 0L, 0L, 200)
        val baseRemoteEvent1 = EventFactory.createRemoteCall("url", "post", "error", 0L, 0L, 401)
        val json = JsonUtil.EVENT_JSON_ADAPTER.toJson(listOf(baseRemoteEvent1, baseRemoteEvent))
        assertNotNull(json)
        assertTrue(json.contains(IdProvider.sessionId))
        assertTrue(json.contains(baseRemoteEvent1.event.timestamp.toString()))
    }

    @Test
    fun parseJsonConfigFile() {
        val json = JsonUtil.CONFIG_JSON_ADAPTER.fromJson(CONFIG)
        assertNotNull(json)
    }

    @Test
    fun returnNullIfNoFileJsonConfigFileOnDisk() {
        assertNull(JsonUtil.getAssetJsonString(app))
    }

    companion object {
        const val CONFIG = """{
                "reportingUrl": "http://10.0.2.2:3000/v1/api",
                "alertFrameRateDipThreshold": 30,
                "alertApplicationNotRespondingThreshold": 3,
                "remoteCallInstrumentationType": 0,
                "suspendReporting": false,
                "key": "42",
                "eventsBufferSize": 200,
                "alertLowMemory": true
        }"""
    }
}