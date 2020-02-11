package com.instana.android.core.util

import com.instana.android.BaseTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class JsonUtilShould : BaseTest() {

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