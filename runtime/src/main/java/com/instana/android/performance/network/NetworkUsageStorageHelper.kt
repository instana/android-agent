/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2025, 2025
 */

package com.instana.android.performance.network

import android.app.Application
import com.instana.android.core.util.SharedPrefsUtil

internal class NetworkUsageStorageHelper(private val appContext: Application) {
    private val startTimeStamp:String = "n_w_start_time"
    private val dataUsedTillTime:String = "data_used_till_date"

    fun isTimeDataAvailable() = !(getStartTime() == 0L || getDataUsed() == 0L)

    fun getStartTime() = SharedPrefsUtil.getLong(appContext, startTimeStamp)

    fun getDataUsed() = SharedPrefsUtil.getLong(appContext, dataUsedTillTime)

    fun saveStartTime(timestamp:Long) = SharedPrefsUtil.putLong(appContext,startTimeStamp,timestamp)

    fun saveStartTimeImmediate(timestamp:Long) = SharedPrefsUtil.putLongImmediate(appContext,startTimeStamp,timestamp)

    fun saveDataUsed(dataBytes:Long) = SharedPrefsUtil.putLong(appContext,dataUsedTillTime,dataBytes)

    fun saveDataUsedImmediate(dataBytes:Long) = SharedPrefsUtil.putLongImmediate(appContext,dataUsedTillTime,dataBytes)

    fun isRebooted(currentNetworkUsedFromReboot:Long) = getDataUsed()!=0L && getDataUsed()>currentNetworkUsedFromReboot


}