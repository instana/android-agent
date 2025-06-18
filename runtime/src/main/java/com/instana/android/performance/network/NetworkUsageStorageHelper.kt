/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.network

import android.app.Application
import com.instana.android.core.util.SharedPrefsUtil

internal class NetworkUsageStorageHelper(private val appContext: Application) {
    private val dataUsedTillTime:String = "data_used_till_date"
    private val backgroundNetworkUsage:String = "instana_background_n_w_usage"

    fun getDataUsed() = SharedPrefsUtil.getLong(appContext, dataUsedTillTime,0L)

    fun saveDataUsed(dataBytes:Long) = SharedPrefsUtil.putLong(appContext,dataUsedTillTime,dataBytes)

    fun saveDataUsedImmediate(dataBytes:Long) = SharedPrefsUtil.putLongImmediate(appContext,dataUsedTillTime,dataBytes)

    fun isRebooted(currentNetworkUsedFromReboot:Long) = getDataUsed() > currentNetworkUsedFromReboot

    fun saveBackgroundNetworkUsage(dataBytes:Long) = SharedPrefsUtil.putLongImmediate(appContext,backgroundNetworkUsage,dataBytes)

    fun getBackgroundNetworkUsage() = SharedPrefsUtil.getLong(appContext,backgroundNetworkUsage,0L)

    fun resetNetworkUsage() {
        saveDataUsedImmediate(0L)
        saveBackgroundNetworkUsage(0L)
    }
}