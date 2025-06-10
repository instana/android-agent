/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.network

import android.app.Application
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.Build
import com.instana.android.core.util.Logger

internal class NetworkStatsHelper(private val appContext: Application) {

    // Get the package name of the current application
    private val packageName: String = appContext.packageName

    private val networkUsageStorageHelper = NetworkUsageStorageHelper(appContext)

    // Function to get UID for the app
    private fun getAppUid(): Int? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION") appContext.packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.applicationInfo.uid
        } catch (e: Exception) {
            Logger.e("App UID not found for package: $packageName", e)
            null
        }
    }

    // Function to track network usage for the current app
    private fun getAppNetworkUsage(): Long {
        return try {
            val uid = getAppUid()
            if (uid != null) {
                val rxBytes = TrafficStats.getUidRxBytes(uid)
                val txBytes = TrafficStats.getUidTxBytes(uid)

                // Handle possible negative values indicating error
                if (rxBytes < 0 || txBytes < 0) {
                    Logger.e("Error retrieving network stats for UID: $uid")
                    return -1L
                }

                // Calculate and log the total usage
                val totalUsage = rxBytes + txBytes
                totalUsage
            } else {
                Logger.e("Unable to retrieve UID for app: $packageName")
                -1L // UID not found
            }
        } catch (e: Exception) {
            // Catch any unexpected exceptions and log the error
            Logger.e("Exception while retrieving network usage: ${e.localizedMessage}")
            -1L // Error occurred during execution
        }
    }


    fun calculateNetworkUsage(fromOnCreate: Boolean) {

        val currentNetworkUsed = getAppNetworkUsage()
        // Guard clause if network usage is invalid
        if (currentNetworkUsed == -1L) return

        // If data is unavailable or reboot detected, save current network usage
        if (shouldSaveInitialData(currentNetworkUsed)) {
            networkUsageStorageHelper.resetNetworkUsage()
            saveNetworkUsage(currentNetworkUsed, fromOnCreate)
            return
        }

        // Calculate the network usage difference
        val dataUsed = currentNetworkUsed - networkUsageStorageHelper.getDataUsed()

        // Save the current usage as a new baseline
        saveNetworkUsage(currentNetworkUsed, fromOnCreate)

        // report the event
        reportNetworkUsage(fromOnCreate, dataUsed)
    }

    private fun shouldSaveInitialData(currentNetworkUsed: Long): Boolean {
        return networkUsageStorageHelper.getDataUsed() == 0L || networkUsageStorageHelper.isRebooted(currentNetworkUsed)
    }

    private fun saveNetworkUsage(currentUsageBytes: Long, isFromOnCreate: Boolean) =
        (if (isFromOnCreate) networkUsageStorageHelper::saveDataUsed else networkUsageStorageHelper::saveDataUsedImmediate)(currentUsageBytes)


    private fun reportNetworkUsage(fromOnCreate: Boolean, dataUsed: Long) {
        //Only save the data used if its background n/w usage
        if (fromOnCreate) {
            val backgroundDataUsedAlready = networkUsageStorageHelper.getBackgroundNetworkUsage()
            networkUsageStorageHelper.saveBackgroundNetworkUsage(backgroundDataUsedAlready + dataUsed)
        }
    }


}