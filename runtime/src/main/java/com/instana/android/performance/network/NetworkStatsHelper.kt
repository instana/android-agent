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

internal class NetworkStatsHelper(private val appContext:Application) {

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
        val currentTimeStamp = System.currentTimeMillis()

        // Guard clause if network usage is invalid
        if (currentNetworkUsed == -1L) return

        // If data is unavailable or reboot detected, save current network usage and time
        if (shouldSaveInitialData(currentNetworkUsed)) {
            saveNetworkUsage(currentNetworkUsed, currentTimeStamp, fromOnCreate)
            return
        }

        // Calculate the network usage difference and time difference
        val timeDiff = currentTimeStamp - networkUsageStorageHelper.getStartTime()
        val dataUsed = currentNetworkUsed - networkUsageStorageHelper.getDataUsed()

        // Save the current usage as a new baseline
        saveNetworkUsage(currentNetworkUsed, currentTimeStamp, fromOnCreate)

        // Log and report the event
        logAndReportNetworkUsage(fromOnCreate, dataUsed, timeDiff)
    }

    private fun shouldSaveInitialData(currentNetworkUsed: Long): Boolean {
        return !networkUsageStorageHelper.isTimeDataAvailable() ||
                networkUsageStorageHelper.isRebooted(currentNetworkUsed)
    }

    private fun saveNetworkUsage(currentNetworkUsed: Long, currentTimeStamp: Long, fromOnCreate: Boolean) {
        val saveMethod = if (fromOnCreate) {
            // Use the regular save methods
            networkUsageStorageHelper::saveDataUsed to networkUsageStorageHelper::saveStartTime
        } else {
            // Use immediate save methods for commit before the app destroy
            networkUsageStorageHelper::saveDataUsedImmediate to networkUsageStorageHelper::saveStartTimeImmediate
        }
        saveMethod.first(currentNetworkUsed)
        saveMethod.second(currentTimeStamp)
    }

    private fun logAndReportNetworkUsage(fromOnCreate: Boolean, dataUsed: Long, timeDiff: Long) {
        val logMessage = "$dataUsed Bytes used in last $timeDiff ms from ${if (fromOnCreate) "Background" else "Foreground"}"
        Logger.i(logMessage)
    }



}