/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.performance.launchtime

import android.app.ActivityManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

internal class StartupInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        if (context == null || !isAppInForeground()) {
            LaunchTimeTracker.applicationStartedFromBackground = true
            return false
        } else {
            LaunchTimeTracker.startTimer()
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        val myProcess = runningAppProcesses.firstOrNull { it.pid == android.os.Process.myPid() }
        return myProcess?.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}