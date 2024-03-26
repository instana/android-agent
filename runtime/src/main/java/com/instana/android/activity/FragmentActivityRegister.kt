/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.instana.android.core.DefaultActivityLifecycleCallbacks

/**
 * Registers the FragmentLifecycleCallbacks when an activity is created. There are just 2 factory
 * methods here, one for API level before 29, and one for the rest.
 */
internal class FragmentActivityRegister {

    fun create(
        fragmentCallbacks: FragmentManager.FragmentLifecycleCallbacks,
    ): ActivityLifecycleCallbacks {
        return object : DefaultActivityLifecycleCallbacks {
            override fun onActivityPreCreated(
                activity: Activity, savedInstanceState: Bundle?,
            ) {
                if (activity is FragmentActivity) {
                    register(activity, fragmentCallbacks)
                }
            }
        }
    }

    fun createPre29(
        fragmentCallbacks: FragmentManager.FragmentLifecycleCallbacks,
    ): ActivityLifecycleCallbacks {
        return object : DefaultActivityLifecycleCallbacks{
            override fun onActivityCreated(
                activity: Activity, savedInstanceState: Bundle?,
            ) {
                if (activity is FragmentActivity) {
                    register(activity, fragmentCallbacks)
                }
            }
        }
    }

    private fun register(
        activity: FragmentActivity,
        fragmentCallbacks: FragmentManager.FragmentLifecycleCallbacks,
    ) {
        val fragmentManager = activity.supportFragmentManager
        fragmentManager.registerFragmentLifecycleCallbacks(fragmentCallbacks, true)
    }
}