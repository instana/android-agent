/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.dropbeaconhandler

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.core.event.models.AppProfile
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.core.event.models.UserProfile
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

class DropBeaconHandlerTest:BaseTest() {
    @Test
    fun `test addBeaconToDropHandler adds beacon to httpUniqueMap if http beacon is provided`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        println(httpMap.toString().contains("https://www.google.com/#q=reprehendunt"))
        println(httpMap.toString())
        Assert.assertTrue(httpMap.toString().contains("https://www.google.com/#q=reprehendunt"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding http beacon if already exist by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        Assert.assertEquals(httpMap["https://www.google.com/#q=reprehendunt|HTTP_VIEW|GET|200|{something=somethingValue}"]?.count?.get(),3)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding http beacon if non existing by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        Assert.assertEquals(httpMap["https://www.google.com/#q=reprehendunt|HTTP_VIEW|GET|200|{something=somethingValue}"]?.count?.get(),1)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding http beacon existing should update time with latest`(){
        val newBeacon = getHttpBeacon()
        newBeacon.setTimestamp(123456)
        DropBeaconHandler.addBeaconToDropHandler(beacon = newBeacon)
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        Assert.assertTrue(httpMap["https://www.google.com/#q=reprehendunt|HTTP_VIEW|GET|200|{something=somethingValue}"].toString().contains("123456"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adds beacon to viewUniqueMap if view beacon is provided`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertTrue(viewMap.toString().contains("eleifend"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding view beacon if already exist by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertEquals(viewMap["eleifend|{}"]?.count?.get(),3)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding view beacon if non existing by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertEquals(viewMap["eleifend|{}"]?.count?.get(),1)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding view beacon existing should update time with latest`(){
        val newBeacon = getViewBeacon()
        newBeacon.setTimestamp(123456)
        DropBeaconHandler.addBeaconToDropHandler(beacon = newBeacon)
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertTrue(viewMap["eleifend|{}"].toString().contains("123456"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adds beacon to customUniqueMap if custom beacon is provided`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        Assert.assertTrue(customMap.toString().contains("Custom Event"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding custom beacon if already exist by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        Assert.assertEquals(customMap["Custom Event|Error message|1|view of events"]?.count?.get(),3)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding custom event beacon if non existing by count check`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        Assert.assertEquals(customMap["Custom Event|Error message|1|view of events"]?.count?.get(),1)
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding custom beacon existing should update time with latest`(){
        val newBeacon = getCustomEventBeacon()
        newBeacon.setTimestamp(123456)
        DropBeaconHandler.addBeaconToDropHandler(beacon = newBeacon)
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        Assert.assertTrue(customMap["Custom Event|Error message|1|view of events"].toString().contains("123456"))
        resetDropBeaconHandler()
    }
    @Test
    fun `test addBeaconToDropHandler adding custom beacon with appended customMetric`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        Assert.assertEquals("3.12,3.12,3.12",httpMap["Custom Event|Error message|1|view of events"]?.customMetric)
        resetDropBeaconHandler()
    }

    @Test
    fun `test flushDropperCustomEvent with all beacon maps having more than 2 records in sum of count will clear the map fields`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.flushDropperCustomEvent()
        Thread.sleep(300)
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertEquals(customMap.size,0)
        Assert.assertEquals(httpMap.size,0)
        Assert.assertEquals(viewMap.size,0)
        resetDropBeaconHandler()
    }

    @Test
    fun `test flushDropperCustomEvent with all beacon maps having less than 2 records in sum of count will clear the map fields and flushing wont be done`(){
        DropBeaconHandler.addBeaconToDropHandler(beacon = getCustomEventBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getViewBeacon())
        DropBeaconHandler.addBeaconToDropHandler(beacon = getHttpBeacon())
        DropBeaconHandler.flushDropperCustomEvent()
        Thread.sleep(400)
        val customMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, CustomEventDropBeacon>
        val httpMap = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        val viewMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, ViewDropBeacon>
        Assert.assertEquals(customMap.size,0)
        Assert.assertEquals(httpMap.size,0)
        Assert.assertEquals(viewMap.size,0)
        resetDropBeaconHandler()
    }

    @Test
    fun `test sendDropperEvent should not send data if lastSendMaps is same as new map`(){
        val customBeacon = getCustomEventBeacon()
        val httpBeacon = getHttpBeacon()
        val viewBeacon = getViewBeacon()
        fun addBeacons(vararg beacons: Beacon) {
            beacons.forEach { beacon ->
                repeat(3) {
                    DropBeaconHandler.addBeaconToDropHandler(beacon)
                }
            }
        }
        addBeacons(customBeacon)
        addBeacons(viewBeacon)
        addBeacons(httpBeacon)
        DropBeaconHandler.flushDropperCustomEvent()
        Thread.sleep(300)
        val lastSendMapBefore = getPrivateFieldValue(DropBeaconHandler,"lastSendMap") as Map<String,String>
        addBeacons(customBeacon)
        addBeacons(viewBeacon)
        addBeacons(httpBeacon)
        DropBeaconHandler.flushDropperCustomEvent()
        Thread.sleep(300)
        val lastSendMapAfter = getPrivateFieldValue(DropBeaconHandler,"lastSendMap") as Map<String,String>
        Assert.assertEquals(lastSendMapBefore.values.toString(),lastSendMapAfter.values.toString())
        resetDropBeaconHandler()
    }

    @Test
    fun `test droppingStartView should only set once in a drop event`(){
        setPrivateField(DropBeaconHandler,"droppingStartView",null)
        invokePrivateMethod2(DropBeaconHandler,"updateDroppingStartView","View",String::class.java)
        invokePrivateMethod2(DropBeaconHandler,"updateDroppingStartView","View3",String::class.java)
        invokePrivateMethod2(DropBeaconHandler,"updateDroppingStartView","View5",String::class.java)
        val droppingStartViewValue = getPrivateFieldValue(DropBeaconHandler,"droppingStartView") as String
        Assert.assertEquals(droppingStartViewValue,"View")
        resetDropBeaconHandler()
    }

    @Test
    fun `test droppingStartView should reset if null is provided`(){
        invokePrivateMethod2(DropBeaconHandler,"updateDroppingStartView","View",String::class.java)
        setPrivateField(DropBeaconHandler,"droppingStartView",null)
        invokePrivateMethod2(DropBeaconHandler,"updateDroppingStartView","View5",String::class.java)
        val droppingStartViewValue = getPrivateFieldValue(DropBeaconHandler,"droppingStartView") as String
        Assert.assertEquals(droppingStartViewValue,"View5")
        resetDropBeaconHandler()
    }

    private fun getHttpBeacon():Beacon{
        return Beacon.newHttpRequest(
            appKey = "nobis",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.ETHERNET, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "finibus",
            view = "HTTP_VIEW",
            meta = mapOf(),
            duration = 5985,
            method = "GET",
            url = "https://www.google.com/#q=reprehendunt",
            headers = mapOf("something" to "somethingValue"),
            backendTraceId = null,
            responseCode = 200,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = null
        )
    }

    private fun getViewBeacon():Beacon{
        return Beacon.newViewChange(
            appKey = "malesuada",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.ETHERNET, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "vocent",
            view = "eleifend",
            meta = mapOf(),
            viewMeta = mapOf("view_key" to "view_Value")
        )
    }

    private fun getCustomEventBeacon():Beacon{
        return Beacon.newCustomEvent(
            appKey = "feugait",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.ETHERNET, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "dictumst",
            view = "view of events",
            meta = mapOf(),
            startTime = 3838,
            duration = 4750,
            backendTraceId = null,
            error = "Error message",
            name = "Custom Event",
            customMetric = 3.12
        )
    }

    private fun resetDropBeaconHandler(){
        val httpUniqueHash = getPrivateFieldValue(DropBeaconHandler,"httpUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        httpUniqueHash.clear()
        val viewUniqueMap = getPrivateFieldValue(DropBeaconHandler,"viewUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        viewUniqueMap.clear()
        val customUniqueMap = getPrivateFieldValue(DropBeaconHandler,"customUniqueMap") as ConcurrentHashMap<String, HttpDropBeacon>
        customUniqueMap.clear()
    }

}