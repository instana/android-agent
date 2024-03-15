/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.event.models

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.core.InstanaConfig
import com.instana.android.session.SessionService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.FileInputStream
import java.util.Properties

class BeaconTest: BaseTest() {
    private lateinit var agentVersion:String
    @Before
    fun `test setup`(){
        val gradleProperties =  Properties()
        val gradlePropertiesFile =  FileInputStream("../version.gradle")
        gradleProperties.load(gradlePropertiesFile)
        agentVersion = gradleProperties.getProperty("ext.agent_version")
        gradlePropertiesFile.close()
    }

    @Test
    fun `test beacons class with new session start auto add beacon type`(){
        val sessionStart = Beacon.newSessionStart(
            appKey = "pertinax",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = DeviceProfile(
                platform = Platform.ANDROID,
                osName = null,
                osVersion = null,
                deviceManufacturer = null,
                deviceModel = null,
                deviceHardware = null,
                rooted = null,
                locale = null,
                viewportWidth = null,
                viewportHeight = null,
                googlePlayServicesMissing = null
            ),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType = EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "test",
            view = null,
            meta = mapOf()
        )
        assert(sessionStart.toString().contains("t\tsessionStart"))
    }

    @Test
    fun `test beacons class with new custom event auto add beacon type`(){
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "pharetra",
            view = null,
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = null,
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        assert(customEvent.toString().contains("t\tcustom"))
    }

    @Test
    fun `test beacons class with setMobileFeatures list is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setMobileFeatures(listOf(MobileFeature.CRASH))
        assert(customEvent.toString().contains("uf\tc"))
    }

    @Test
    fun `test beacons class with BatchSize is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setBatchSize(5)
        assert(customEvent.toString().contains("bs\t5"))
    }

    @Test
    fun `test beacons class with user data is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        Instana.userId = "id"
        Instana.userName = "Usr name"
        Instana.userEmail = "Usr email"
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        assert(customEvent.toString().contains("un\tUsr name"))
        assert(customEvent.toString().contains("ue\tUsr email"))
        assert(customEvent.toString().contains("ui\tid"))
    }

    @Test
    fun `test beacons class with google play service missing data is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = DeviceProfile(
                platform =Platform.ANDROID,
                osName = null,
                osVersion = null,
                deviceManufacturer = null,
                deviceModel = null,
                deviceHardware = null,
                rooted = null,
                locale = null,
                viewportWidth = null,
                viewportHeight = null,
                googlePlayServicesMissing = null
            ),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setGooglePlayServicesMissing(true)
        assert(customEvent.toString().contains("gpsm\ttrue"))
    }

    @Test
    fun `test beacons class with header capture key and value is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = DeviceProfile(
                platform =Platform.ANDROID,
                osName = null,
                osVersion = null,
                deviceManufacturer = null,
                deviceModel = null,
                deviceHardware = null,
                rooted = null,
                locale = null,
                viewportWidth = null,
                viewportHeight = null,
                googlePlayServicesMissing = null
            ),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setHttpCallHeaders("test_key","test_value")
        assert(customEvent.toString().contains("h_test_key\ttest_value"))
    }

    @Test
    fun `test beacons class with decoded body size is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setDecodedBodySize(3)
        assert(customEvent.toString().contains("dbs\t3"))
    }

    @Test
    fun `test beacons class with set transfer size is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setTransferSize(5)
        assert(customEvent.toString().contains("trs\t5"))
    }

    @Test
    fun `test beacons class with set error type is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        customEvent.setErrorType("ERROR_TYPE_TEST")
        assert(customEvent.toString().contains("et\tERROR_TYPE_TEST"))
    }

    @Test
    fun `test beacons class with setHttpCallMethod truncate mote than 16 character`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        val hm = "12345678901234567890"
        customEvent.setHttpCallMethod(hm)
        Assert.assertFalse(customEvent.toString().contains("hm\t$hm"))
    }

    @Test
    fun `test beacons class with add meta data is been added`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = false)
        Instana.sessionId = null
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        Instana.meta.put("test" ,"test")
        val finalBeacon = Beacon.addMetaData(customEvent.toString(),"test_key","test_value")
        Assert.assertTrue(finalBeacon.contains("m_test_key\ttest_value"))
    }

    @Test
    fun `test version name of agent is taken with hybrid apps`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        Instana.sessionId = null
        Instana.setup(app,config)
        SessionService(app,mockWorkManager,config)
        Instana.config?.hybridAgentId = "agent_id"
        Instana.config?.hybridAgentVersion = "1.0.1"
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        Assert.assertTrue(customEvent.toString().contains("agv\t${agentVersion.replace("'","")}:agent_id:1.0.1"))
    }
    
    @Test
    fun `test usiRefreshTimeIntervalInHrs if not 0 then report usi`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        config.usiRefreshTimeIntervalInHrs = 2
        Instana.sessionId = null
        Instana.setup(app,config)
        SessionService(app,mockWorkManager,config)
        Instana.config?.hybridAgentId = "agent_id"
        Instana.config?.hybridAgentVersion = "1.0.1"
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        Assert.assertTrue(customEvent.toString().contains("usi"))
    }

    @Test
    fun `test usiRefreshTimeIntervalInHrs if is 0L then report usi`(){
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, enableCrashReporting = true)
        config.usiRefreshTimeIntervalInHrs = 0
        Instana.sessionId = null
        Instana.setup(app,config)
        SessionService(app,mockWorkManager,config)
        val customEvent = Beacon.newCustomEvent(
            appKey = "maiorum",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = Instana.deviceProfile,
            connectionProfile = ConnectionProfile(carrierName = null, connectionType = ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId?:"test",
            view = "null",
            meta = mapOf(),
            startTime = 9413,
            duration = 7629,
            backendTraceId = "null",
            error = null,
            name = "Raymond Griffith",
            customMetric = null
        )
        Assert.assertFalse(customEvent.toString().contains("usi"))
    }

    @Test
    fun `test get view meta data`(){
        val viewBeacon = Beacon.newViewChange(
            appKey = "electram",
            appProfile = AppProfile(appVersion = null, appBuild = null, appId = null),
            deviceProfile = DeviceProfile(
                platform =Platform.ANDROID,
                osName = null,
                osVersion = null,
                deviceManufacturer = null,
                deviceModel = null,
                deviceHardware = null,
                rooted = null,
                locale = null,
                viewportWidth = null,
                viewportHeight = null,
                googlePlayServicesMissing = null
            ),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_2G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "causae",
            view = "errem",
            meta = mapOf(),
            viewMeta = mapOf("testKey" to "value")
        )
        Assert.assertEquals(viewBeacon.getViewMeta("testKey"),"value")
    }

    @Test
    fun `test http request beacon condition with error as null will have error count 0`(){
        val httpBeacon = Beacon.newHttpRequest(
            appKey = "tristique",
            appProfile = AppProfile(),
            deviceProfile = DeviceProfile(),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "elaboraret",
            view = null,
            meta = mapOf(),
            duration = 7890,
            method = null,
            url = "https://duckduckgo.com/?q=discere",
            headers = mapOf(),
            backendTraceId = null,
            responseCode = null,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = null
        )
        assert(httpBeacon.toString().contains("ec\t0"))
    }

    @Test
    fun `test http request beacon condition with error as non null will have error count 1`(){
        val httpBeacon = Beacon.newHttpRequest(
            appKey = "tristique",
            appProfile = AppProfile(),
            deviceProfile = DeviceProfile(),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "elaboraret",
            view = null,
            meta = mapOf(),
            duration = 7890,
            method = null,
            url = "https://duckduckgo.com/?q=discere",
            headers = mapOf("test" to "tests"),
            backendTraceId = null,
            responseCode = null,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = "something"
        )
        assert(httpBeacon.toString().contains("ec\t1"))
    }

    @Test
    fun `test http request beacon condition with response code from 400 to 599 wil have error count count 1`(){
        val httpBeacon = Beacon.newHttpRequest(
            appKey = "tristique",
            appProfile = AppProfile(),
            deviceProfile = DeviceProfile(),
            connectionProfile = ConnectionProfile(carrierName = null, connectionType =ConnectionType.CELLULAR, effectiveConnectionType =EffectiveConnectionType.TYPE_4G),
            userProfile = UserProfile(userId = null, userName = null, userEmail = null),
            sessionId = "elaboraret",
            view = "null",
            meta = mapOf(),
            duration = 7890,
            method = null,
            url = "https://duckduckgo.com/?q=discere",
            headers = mapOf(),
            backendTraceId = null,
            responseCode = 401,
            requestSizeBytes = null,
            encodedResponseSizeBytes = null,
            decodedResponseSizeBytes = null,
            error = null
        )
        assert(httpBeacon.toString().contains("ec\t1"))
    }
    
    @Test
    fun `test retrieveVersionName with default agent android`(){
        val beacon = mock(Beacon::class.java)
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,agentVersion.replace("'",""))
    }

    @Test
    fun `test retrieveVersionName with hybridAgentId as empty string versions`(){
        val beacon = mock(Beacon::class.java)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        Instana.sessionId = null
        Instana.setup(app,config)
        Instana.config?.hybridAgentId = ""
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,agentVersion.replace("'",""))
    }

    @Test
    fun `test retrieveVersionName with hybridAgentId as null versions`(){
        val beacon = mock(Beacon::class.java)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        Instana.sessionId = null
        Instana.setup(app,config)
        Instana.config?.hybridAgentId = null
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,agentVersion.replace("'",""))
    }

    @Test
    fun `test initWorkManager should return without collection`(){
        val beacon = mock(Beacon::class.java)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, collectionEnabled = false)
        Instana.sessionId = null
        Instana.setup(app,config)
        Instana.setCollectionEnabled(false)
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,agentVersion.replace("'",""))
    }

    @Test
    fun `test retrieveVersionName with hybridAgentId as Android versions`(){
        val beacon = mock(Beacon::class.java)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        Instana.sessionId = null
        Instana.setup(app,config)
        Instana.config?.hybridAgentId = Platform.ANDROID.internalType
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,agentVersion.replace("'",""))
    }

    @Test
    fun `test retrieveVersionName with hybridAgentId with version`(){
        val beacon = mock(Beacon::class.java)
        val config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL)
        Instana.sessionId = null
        Instana.setup(app,config)
        Instana.config?.hybridAgentId = "f"
        Instana.config?.hybridAgentVersion = "2.4.3"
        val response = invokePrivateMethod(beacon,"retrieveVersionName") as String
        Assert.assertEquals(response ,"${agentVersion.replace("'","")}:f:2.4.3")
    }


}