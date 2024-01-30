package com.instana.android.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
/**
 * Interface helper so that needed callbacks can be utilised instead of ending in adding up unwanted overrides
 */
interface DefaultActivityLifecycleCallbacks: Application.ActivityLifecycleCallbacks{

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}