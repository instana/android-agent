/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.instana.android.Instana
import com.instana.android.activity.FragmentActivityRegister
import com.instana.android.activity.InstanaActivityLifecycleCallbacks
import com.instana.android.core.util.ConstantsAndUtil
import com.instana.android.fragments.FragmentLifecycleCallbacks
import com.instana.android.performance.launchtime.LaunchTimeTracker
import com.instana.android.performance.launchtime.LaunchTypeEnum
import com.instana.android.performance.network.AppLifecycleIdentificationService
import com.instana.android.performance.network.NetworkUsageWorker
import java.util.concurrent.TimeUnit

/**
 * Util class to get current activity data and memory alerts
 */
class InstanaLifeCycle(
    application: Application,
) : DefaultActivityLifecycleCallbacks, ComponentCallbacks2 {

    private var callback: AppStateCallback? = null
    private var backgrounded: Boolean = false
    private var activityCount = 0  // Track active activities
    private var isConfigurationChange = false  // Track if the activity is just being recreated
    /**
     * Public variable that provides activity name for reports that require it
     */
    var activityName: String? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        application.registerComponentCallbacks(this)
        if(Instana.config?.autoCaptureScreenNames == true){
            application.registerActivityLifecycleCallbacks(InstanaActivityLifecycleCallbacks())
            registerFragmentCallbacks(application)
        }
        if(ConstantsAndUtil.isBackgroundEnuEnabled() && !backgrounded){
            application.startService(Intent(application, AppLifecycleIdentificationService::class.java))
            startTheDailyWorkerForENU()
        }
    }


    private fun startTheDailyWorkerForENU(){
        val workRequest = PeriodicWorkRequestBuilder<NetworkUsageWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        Instana.getApplication()?.let {
            WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                "InstanaEnuWork",
                ExistingPeriodicWorkPolicy.KEEP, // prevent duplication
                workRequest
            )
        }

    }

    override fun onLowMemory() {
        // not implemented
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        isConfigurationChange = true
    }

    /**
     * Receive alerts callback for memory status here
     */
    override fun onTrimMemory(level: Int) {
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            callback?.onAppInBackground()
            backgrounded = true
        }
    }

    override fun onActivityPaused(activity: Activity) {
        activityName = activity.localClassName.toString()
    }

    override fun onActivityResumed(activity: Activity) {
        if(LaunchTimeTracker.applicationStartedFromBackground){
            LaunchTimeTracker.stopTimer(LaunchTypeEnum.WARM_START)
        }else{
            LaunchTimeTracker.stopTimer(LaunchTypeEnum.COLD_START)
        }
        if (backgrounded) {
            backgrounded = false
            callback?.onAppInForeground()
        }
        activityName = activity.localClassName.toString()
    }

    override fun onActivityStarted(activity: Activity) {
        if(LaunchTimeTracker.applicationStartedFromBackground){
            LaunchTimeTracker.startTimer()
        }
        activityName = activity.localClassName.toString()
        activityCount++
    }

    override fun onActivityDestroyed(activity: Activity) {
        activityName = activity.localClassName.toString()
        activityCount--
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        activityName = activity.localClassName.toString()
    }

    override fun onActivityStopped(activity: Activity) {
        activityName = activity.localClassName.toString()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activityName = activity.localClassName.toString()
        if(activityCount==0 && !isConfigurationChange && ConstantsAndUtil.isBackgroundEnuEnabled()){
            Instana.config?.networkStatsHelper?.calculateNetworkUsage(true)
        }
        isConfigurationChange = false
    }

    fun registerCallback(appStateCallback: AppStateCallback) {
        this.callback = appStateCallback
    }

    private fun registerFragmentCallbacks(application: Application){
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val fragmentActivityRegister = FragmentActivityRegister()
         application.registerActivityLifecycleCallbacks(
             if (Build.VERSION.SDK_INT < 29) {
                 fragmentActivityRegister.createPre29(fragmentLifecycleCallbacks)
             }
             else{
                 fragmentActivityRegister.create(fragmentLifecycleCallbacks)
             }
         )
    }

    interface AppStateCallback {

        fun onAppInBackground()

        fun onAppInForeground()
    }
}