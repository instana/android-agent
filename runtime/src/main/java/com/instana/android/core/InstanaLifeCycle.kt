package com.instana.android.core

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import android.content.res.Configuration
import android.os.Bundle

/**
 * Util class to get current activity data and memory alerts
 */
class InstanaLifeCycle(
    application: Application
) : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private var callback: AppStateCallback? = null
    private var backgrounded: Boolean = false

    /**
     * Public variable that provides activity name for reports that require it
     */
    var activityName: String? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        application.registerComponentCallbacks(this)
    }

    override fun onLowMemory() {
        // not implemented
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        // not implemented
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

    override fun onActivityPaused(activity: Activity?) {
        activityName = activity?.localClassName.toString()
    }

    override fun onActivityResumed(activity: Activity?) {
        if (backgrounded) {
            backgrounded = false
            callback?.onAppInForeground()
        }
        activityName = activity?.localClassName.toString()
    }

    override fun onActivityStarted(activity: Activity?) {
        activityName = activity?.localClassName.toString()
    }

    override fun onActivityDestroyed(activity: Activity?) {
        activityName = activity?.localClassName.toString()
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        activityName = activity?.localClassName.toString()
    }

    override fun onActivityStopped(activity: Activity?) {
        activityName = activity?.localClassName.toString()
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        activityName = activity?.localClassName.toString()
    }

    fun registerCallback(appStateCallback: AppStateCallback) {
        this.callback = appStateCallback
    }

    interface AppStateCallback {

        fun onAppInBackground()

        fun onAppInForeground()
    }
}