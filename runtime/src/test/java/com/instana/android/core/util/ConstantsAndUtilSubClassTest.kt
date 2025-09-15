/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.instana.android.core.event.models.ConnectionType
import com.instana.android.core.event.models.EffectiveConnectionType
import com.instana.android.core.util.ConstantsAndUtil.validateAllKeys
import com.instana.android.view.ScreenAttributes
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.reflect.Whitebox

@Suppress("DEPRECATION")
@RunWith(PowerMockRunner::class)
@PrepareForTest(
    ActivityCompat::class,
    ConstantsAndUtil::class // Add any other classes here
)
class ConstantsAndUtilSubClassTest {
    private val context = Mockito.mock(Context::class.java)
    private val connectivityManager = Mockito.mock(ConnectivityManager::class.java)
    private val telephonyManager = Mockito.mock(TelephonyManager::class.java)

    private fun getConnectivitySetup() {
        PowerMockito.mockStatic(ActivityCompat::class.java)
        PowerMockito.mockStatic(ConstantsAndUtil::class.java)
        Mockito.`when`(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE))
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        val networkInfo = NetworkInfo(1, 2, "something", "something else")
        Mockito.`when`(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
    }

    @Test
    fun `test get connectivity with build version less than 23`() {
        getConnectivitySetup()
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 14)
        val connectionType = ConstantsAndUtil.getConnectionType(context, connectivityManager)
        Assert.assertNotNull(connectionType)
    }

    @Test
    fun `test get connectivity with build version greater than 23 with TRANSPORT_ETHERNET capability`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val connectionType = ConstantsAndUtil.getConnectionType(context, connectivityManager)
        Assert.assertNotNull(connectionType)
    }

    @Test
    fun `test get connectivity with build version greater than 23 with TRANSPORT_WIFI capability`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val connectionType = ConstantsAndUtil.getConnectionType(context, connectivityManager)
        Assert.assertNotNull(connectionType)
    }

    @Test
    fun `test get connectivity with build version greater than 23 with TRANSPORT_CELLULAR capability`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val connectionType = ConstantsAndUtil.getConnectionType(context, connectivityManager)
        Assert.assertNotNull(connectionType)
    }

    @Test
    fun `test get connectivity with build version greater than 23 with throw security exception`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenThrow(SecurityException(""))
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val connectionType = ConstantsAndUtil.getConnectionType(context, connectivityManager)
        Assert.assertNull(connectionType)
    }


    @Test
    fun `test get Cellular Connection Type when NETWORK_TYPE_IDEN has 2G`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_IDEN)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager) as EffectiveConnectionType
        assert(effectiveConnectionType == EffectiveConnectionType.TYPE_2G)
    }

    @Test
    fun `test get Cellular Connection Type when NETWORK_TYPE_HSPAP has 3G`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_HSPAP)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager) as EffectiveConnectionType
        assert(effectiveConnectionType == EffectiveConnectionType.TYPE_3G)
    }

    @Test
    fun `test get Cellular Connection Type when NETWORK_TYPE_IWLAN will return null`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_IWLAN)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager)
        assert(effectiveConnectionType == null)
    }

    @Test
    fun `test get Cellular Connection Type return null when no permission`() {
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_IWLAN)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        PowerMockito.mockStatic(ActivityCompat::class.java)
        Mockito.`when`(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE))
            .thenReturn(PackageManager.PERMISSION_DENIED)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager)
        assert(effectiveConnectionType == null)
    }

    @Test
    fun `test get Cellular Connection Type when NETWORK_TYPE_LTE has 4G`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager) as EffectiveConnectionType
        assert(effectiveConnectionType == EffectiveConnectionType.TYPE_4G)
    }

    @Test
    fun `test get Cellular Connection Type when all types doesnt match return null`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(false)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(false)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)).thenReturn(false)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager)
        assert(effectiveConnectionType == null)
    }

    @Test
    fun `test get Cellular Connection Type when capability set is null`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.networkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager)
        assert(effectiveConnectionType == null)
    }

    @Test
    fun `test get Cellular Connection Type when NETWORK_TYPE_LTE has 4G and build version greater than 24`() {
        getConnectivitySetup()
        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)
        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(telephonyManager.dataNetworkType).thenReturn(TelephonyManager.NETWORK_TYPE_LTE)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)).thenReturn(true)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 24)
        val effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(context, connectivityManager, telephonyManager) as EffectiveConnectionType
        assert(effectiveConnectionType == EffectiveConnectionType.TYPE_4G)
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 23)
    }

    @Test
    fun `test validateAllKeys on a map should give empty response if the map has a invalid key`() {
        val viewMetaMock = mapOf<String, String>(Pair("something_not_valid", "something"), Pair("something", "something"))
        Assert.assertEquals(viewMetaMock.validateAllKeys(), emptyMap<String, String>())
    }

    @Test
    fun `test validateAllKeys on a map should give same map as response if the map has a key from flutter`() {
        val viewMetaMock = mapOf<String, String>(
            Pair("go.router.path", "something"),
            Pair("child.widget.title", "something"),
            Pair("widget.name", "something"),
            Pair("settings.route.name", "something"),
            Pair("child.widget.name", "something")
        )
        Assert.assertEquals(viewMetaMock.validateAllKeys(), viewMetaMock)
    }

    @Test
    fun `test validateAllKeys on a map should give same map as response if the map has a key from Android Agent`() {
        val viewMetaMock = mapOf<String, String>(
            Pair(ScreenAttributes.ACTIVITY_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_ACTIVE_SCREENS_LIST.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value, "something"),
        )
        Assert.assertEquals(viewMetaMock.validateAllKeys(), viewMetaMock)
    }

    @Test
    fun `test validateAllKeys on a map should give same map as response if the map has a key from Android Agent & Flutter agent`() {
        val viewMetaMock = mapOf<String, String>(
            Pair(ScreenAttributes.ACTIVITY_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_ACTIVE_SCREENS_LIST.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value, "something"),
            Pair("go.router.path", "something"),
            Pair("child.widget.title", "something"),
            Pair("widget.name", "something"),
            Pair("settings.route.name", "something"),
            Pair("child.widget.name", "something")
        )
        Assert.assertEquals(viewMetaMock.validateAllKeys(), viewMetaMock)
    }

    @Test
    fun `test validateAllKeys on a map should give empty map as response if the map has at least 1 invalid key with valid android keys`() {
        val viewMetaMock = mapOf<String, String>(
            Pair(ScreenAttributes.ACTIVITY_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_SCREEN_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_CLASS_NAME.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_ACTIVE_SCREENS_LIST.value, "something"),
            Pair(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value, "something"),
            Pair("invalid_key","Invalid_value")
        )
        Assert.assertEquals(viewMetaMock.validateAllKeys(), emptyMap<String,String>())
    }

    @Test
    fun `test validateAllKeys on a map should give empty map as response if the map has at least 1 invalid key with valid flutter keys`() {
        val viewMetaMock = mapOf<String, String>(
            Pair("go.router.path", "something"),
            Pair("child.widget.title", "something"),
            Pair("widget.name", "something"),
            Pair("settings.route.name", "something"),
            Pair("child.widget.name", "something"),
            Pair("child.widget.name.invalid", "something")//invalid key with all other valid keys
        )
        Assert.assertEquals(viewMetaMock.validateAllKeys(), emptyMap<String, String>())
    }

    @Test
    fun `getConnectionProfile - API 23 or above, WIFI`() {
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 30)

        Mockito.`when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        Mockito.`when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)

        val network = Mockito.mock(Network::class.java)
        val networkCapabilities = Mockito.mock(NetworkCapabilities::class.java)

        Mockito.`when`(connectivityManager.activeNetwork).thenReturn(network)
        Mockito.`when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)
        Mockito.`when`(networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)

        PowerMockito.mockStatic(ActivityCompat::class.java)
        PowerMockito.`when`(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
        ).thenReturn(PackageManager.PERMISSION_GRANTED)

        // DO NOT mock ConstantsAndUtil.getConnectionType here! Let real method run!

        val profile = ConstantsAndUtil.getConnectionProfile(context)

        Assert.assertNull(profile.carrierName)
        Assert.assertEquals(ConnectionType.WIFI, profile.connectionType)
        Assert.assertNull(profile.effectiveConnectionType)
    }


    @Test
    fun `getConnectionProfile - TelephonyManager is null - API below 23`() {
        // Set SDK_INT < 23 to test old code path
        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 22)

        // Setup system services
        Mockito.`when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)
        Mockito.`when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)

        // Mock activeNetworkInfo and its type
        val networkInfo = Mockito.mock(NetworkInfo::class.java)
        Mockito.`when`(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        Mockito.`when`(networkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI)

        // Mock permission check (ACCESS_NETWORK_STATE granted)
        PowerMockito.mockStatic(ActivityCompat::class.java)
        Mockito.`when`(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)
        ).thenReturn(PackageManager.PERMISSION_GRANTED)

        // Mock your static methods in ConstantsAndUtil
        PowerMockito.mockStatic(ConstantsAndUtil::class.java)
        PowerMockito.`when`(
            ConstantsAndUtil.getConnectionType(context, connectivityManager)
        ).thenCallRealMethod()  // call real method since dependencies are mocked correctly

        // Call the real getConnectionProfile method (assuming it uses getConnectionType internally)
        val profile = ConstantsAndUtil.getConnectionProfile(context)

        // Validate results
        Assert.assertNull(profile.carrierName)  // TelephonyManager is null, so carrierName null
        Assert.assertEquals(ConnectionType.CELLULAR, profile.connectionType)
        Assert.assertNull(profile.effectiveConnectionType)
    }


    @Test
    fun `getConnectionProfile - ConnectivityManager is null`() {
        Mockito.`when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(null)
        Mockito.`when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)

        val profile = ConstantsAndUtil.getConnectionProfile(context)

        Assert.assertNull(profile.carrierName)
        Assert.assertNull(profile.connectionType)
        Assert.assertNull(profile.effectiveConnectionType)
    }

    @Test
    fun `getConnectionProfile - both ConnectivityManager and TelephonyManager are null`() {
        Mockito.`when`(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(null)
        Mockito.`when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)

        val profile = ConstantsAndUtil.getConnectionProfile(context)

        Assert.assertNull(profile.carrierName)
        Assert.assertNull(profile.connectionType)
        Assert.assertNull(profile.effectiveConnectionType)
    }

}
