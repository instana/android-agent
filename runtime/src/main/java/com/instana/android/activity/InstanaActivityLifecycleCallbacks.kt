/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import com.instana.android.Instana
import com.instana.android.core.DefaultActivityLifecycleCallbacks
import com.instana.android.view.VisibleScreenNameTracker
import com.instana.android.view.models.FragmentActivityViewDataModel
import java.util.WeakHashMap

internal class InstanaActivityLifecycleCallbacks:DefaultActivityLifecycleCallbacks {

    // Store timestamps for each activity instance
    private val activityStartTimes = WeakHashMap<Activity, Long>()
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
        if (Build.VERSION.SDK_INT >= 29) {
            activityStartTimes[activity] = System.currentTimeMillis()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (Build.VERSION.SDK_INT < 29) {
            activityStartTimes[activity] = System.currentTimeMillis()
        }
    }
    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        val duration = activityStartTimes.remove(activity)
            ?.let { System.currentTimeMillis() - it }
            ?: 0L
        if(Instana.config?.autoCaptureScreenNames==false) return
        updateScreenTracker(activity,duration)
    }

    private fun updateScreenTracker(activity:Activity,duration: Long=0L){
        val activityName = activity.localClassName
        val activitySimpleName = activity.javaClass.simpleName
        VisibleScreenNameTracker.updateActivityFragmentViewData(FragmentActivityViewDataModel(
            activityClassName = activitySimpleName,
            activityLocalPathName = activityName,
            customActivityScreenName = activity.findContentDescription()?:activitySimpleName,
            screenRenderingDuration = duration
        ))
    }

}