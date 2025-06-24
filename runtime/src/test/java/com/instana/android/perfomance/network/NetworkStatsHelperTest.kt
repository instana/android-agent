/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.perfomance.network

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.TrafficStats
import com.instana.android.BaseTest
import com.instana.android.performance.network.NetworkStatsHelper
import com.instana.android.performance.network.NetworkUsageStorageHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class NetworkStatsHelperTest : BaseTest() {

    private lateinit var context: Application
    private lateinit var packageManager: PackageManager
    private lateinit var helper: NetworkStatsHelper

    @Before
    fun setUp() {
        context = mock(Application::class.java)
        packageManager = mock(PackageManager::class.java)
        `when`(context.packageName).thenReturn("com.example.test")
        `when`(context.packageManager).thenReturn(packageManager)

        helper = NetworkStatsHelper(context)
    }

    @Test
    fun `getAppUid returns uid when package exists`() {
        val mockPackageInfo = PackageInfo()
        val appInfo = ApplicationInfo().apply { uid = 12345 }
        mockPackageInfo.applicationInfo = appInfo

        `when`(packageManager.getPackageInfo(eq("com.example.test"), anyInt())).thenReturn(mockPackageInfo)

        val uid = invokePrivateMethod(helper, "getAppUid") as Int?
        assertEquals(12345, uid)
    }

    @Test
    fun `getAppUid returns null when exception occurs`() {
        `when`(packageManager.getPackageInfo(anyString(), anyInt())).thenThrow(RuntimeException("boom"))

        val uid = invokePrivateMethod(helper, "getAppUid") as Int?
        assertNull(uid)
    }

    @Test
    fun `getAppNetworkUsage returns total usage when stats are valid`() {
        // Setup
        val mockPackageInfo = PackageInfo()
        val appInfo = ApplicationInfo().apply { uid = 123 }
        mockPackageInfo.applicationInfo = appInfo
        `when`(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(mockPackageInfo)

        mockStatic(TrafficStats::class.java).use { trafficMock ->
            trafficMock.`when`<Long> { TrafficStats.getUidRxBytes(123) }.thenReturn(1000L)
            trafficMock.`when`<Long> { TrafficStats.getUidTxBytes(123) }.thenReturn(2000L)

            val usage = invokePrivateMethod(helper, "getAppNetworkUsage") as Long
            assertEquals(3000L, usage)
        }
    }

    @Test
    fun `getAppNetworkUsage returns -1 when UID is null`() {
        // Skip mocking packageManager to simulate exception -> null UID
        val usage = invokePrivateMethod(helper, "getAppNetworkUsage") as Long
        assertEquals(-1L, usage)
    }

    @Test
    fun `getAppNetworkUsage returns -1 when rxBytes or txBytes is negative`() {
        val mockPackageInfo = PackageInfo()
        val appInfo = ApplicationInfo().apply { uid = 123 }
        mockPackageInfo.applicationInfo = appInfo
        `when`(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(mockPackageInfo)

        mockStatic(TrafficStats::class.java).use { trafficMock ->
            trafficMock.`when`<Long> { TrafficStats.getUidRxBytes(123) }.thenReturn(-1L)
            trafficMock.`when`<Long> { TrafficStats.getUidTxBytes(123) }.thenReturn(1000L)

            val usage = invokePrivateMethod(helper, "getAppNetworkUsage") as Long
            assertEquals(-1L, usage)
        }
    }

    @Test
    fun `shouldSaveInitialData returns true when data used is 0`() {
        val storageHelper = mock(NetworkUsageStorageHelper::class.java)
        `when`(storageHelper.getDataUsed()).thenReturn(0L)

        setPrivateField(helper, "networkUsageStorageHelper", storageHelper)
        val result = invokePrivateMethod2(helper, "shouldSaveInitialData", 1234L, Long::class.java) as Boolean

        assertTrue(result)
    }

    @Test
    fun `shouldSaveInitialData returns true when reboot detected`() {
        val storageHelper = mock(NetworkUsageStorageHelper::class.java)
        `when`(storageHelper.getDataUsed()).thenReturn(5000L)
        `when`(storageHelper.isRebooted(1234L)).thenReturn(true)

        setPrivateField(helper, "networkUsageStorageHelper", storageHelper)
        val result = invokePrivateMethod2(helper, "shouldSaveInitialData", 1234L, Long::class.java) as Boolean

        assertTrue(result)
    }

    @Test
    fun `shouldSaveInitialData returns false when not rebooted and data exists`() {
        val storageHelper = mock(NetworkUsageStorageHelper::class.java)
        `when`(storageHelper.getDataUsed()).thenReturn(5000L)
        `when`(storageHelper.isRebooted(1234L)).thenReturn(false)

        setPrivateField(helper, "networkUsageStorageHelper", storageHelper)
        val result = invokePrivateMethod2(helper, "shouldSaveInitialData", 1234L, Long::class.java) as Boolean

        assertFalse(result)
    }

    @Test
    fun `reportNetworkUsage saves background data when fromOnCreate is true`() {
        val storageHelper = mock(NetworkUsageStorageHelper::class.java)
        `when`(storageHelper.getBackgroundNetworkUsage()).thenReturn(200L)
        setPrivateField(helper, "networkUsageStorageHelper", storageHelper)

        invokePrivateMethod3(helper, "reportNetworkUsage", true, Boolean::class.java,200L, Long::class.java)
        verify(storageHelper).saveBackgroundNetworkUsage(400L)
    }

    @Test
    fun `reportNetworkUsage does nothing when fromOnCreate is false`() {
        val storageHelper = mock(NetworkUsageStorageHelper::class.java)
        setPrivateField(helper, "networkUsageStorageHelper", storageHelper)

        invokePrivateMethod3(helper, "reportNetworkUsage", false, Boolean::class.java,200L, Long::class.java)
        verify(storageHelper, never()).saveBackgroundNetworkUsage(anyLong())
    }
}