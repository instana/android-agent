/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.crash

import android.app.Application
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.util.Log
import com.instana.android.Instana
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.core.event.models.Beacon
import com.instana.android.core.event.models.ConnectionProfile
import com.instana.android.core.util.ConstantsAndUtil
import java.util.Queue
import java.util.concurrent.LinkedBlockingDeque

/**
 * Handling crash related actions
 */
class CrashService(
    private val app: Application,
    private val manager: InstanaWorkManager,
    private val config: InstanaConfig,
    private val cm: ConnectivityManager,
    private val tm: TelephonyManager,
    defaultThreadHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler(),
) {

    private val appKey = config.key
    private var breadCrumbs: Queue<String> = LinkedBlockingDeque()
    private var handler: ExceptionHandler? = null

    init {
        if (defaultThreadHandler != null) {
            handler = ExceptionHandler(this, defaultThreadHandler)
            handler?.enable()
        }
    }

    fun leave(breadCrumb: String) {
        breadCrumbs.add(breadCrumb)
        if (breadCrumbs.size > config.breadcrumbsBufferSize) {
            breadCrumbs.poll()
        }
    }

    fun submitCrash(thread: Thread?, throwable: Throwable?) {
        //Condition helps when crash reporting disables at runtime, after init
        if (config.enableCrashReporting.not()) {
            handler?.disable()
            return
        }

        val stackTrace = Log.getStackTraceString(throwable)

        val allStackTraces = ConstantsAndUtil.dumpAllThreads(thread, throwable)

        val errorType:String? = throwable?.javaClass?.name

        val mergedMeta = Instana.meta

        val connectionProfile = ConnectionProfile(
            carrierName = ConstantsAndUtil.getCarrierName(app, cm, tm),
            connectionType = ConstantsAndUtil.getConnectionType(app, cm),
            effectiveConnectionType = ConstantsAndUtil.getCellularConnectionType(app, cm, tm)
        )
        val errorMessage = if (throwable?.message?.isNotBlank() == true) {
            "${throwable.javaClass.name} (${throwable.message})"
        } else throwable?.javaClass?.name

        // send crash
        val beacon = Beacon.newCrash(
            appKey = appKey,
            appProfile = Instana.appProfile,
            deviceProfile = Instana.deviceProfile,
            connectionProfile = connectionProfile,
            userProfile = Instana.userProfile,
            sessionId = Instana.sessionId ?: "",
            view = Instana.view,
            meta = mergedMeta.getAll(),
            error = errorMessage,
            errorType = errorType,
            stackTrace = stackTrace,
            allStackTraces = allStackTraces,
        )
        manager.queueAndFlushBlocking(beacon)
        breadCrumbs.clear()
    }

}
