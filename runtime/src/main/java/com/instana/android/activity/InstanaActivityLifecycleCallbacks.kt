/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import com.instana.android.Instana
import com.instana.android.core.DefaultActivityLifecycleCallbacks
import com.instana.android.core.util.Logger
import com.instana.android.view.VisibleScreenNameTracker
import com.instana.android.view.models.FragmentActivityViewDataModel

internal class InstanaActivityLifecycleCallbacks:DefaultActivityLifecycleCallbacks {

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if(Instana.config?.autoCaptureScreenNames==false) return
        Logger.i("Activity Resumed: ${activity.localClassName}")
        updateScreenTracker(activity)
    }

    private fun updateScreenTracker(activity:Activity){
        val activityName = activity.localClassName
        val activitySimpleName = activity.javaClass.simpleName
        VisibleScreenNameTracker.updateActivityFragmentViewData(FragmentActivityViewDataModel(
            activityClassName = activitySimpleName,
            activityLocalPathName = activityName,
            customActivityScreenName = activity.findContentDescription()?:activitySimpleName,
        ))
    }


}