/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.activity

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import kotlin.math.min

/**
 * Finding the content description from the root layout if provided
 */
internal fun Activity.findContentDescription(): String? {
    val rootView = findViewById<View>(android.R.id.content)
    if (rootView?.contentDescription != null) {
        return rootView.contentDescription.toString()
    }
    //keeping it to root 2 layouts as much deeper layout content descriptions can cause focus on the view elements instead of view
    if (rootView is ViewGroup) {
        for (i in 0 until min(2, rootView.childCount)) {
            val childContentDescription = rootView.getChildAt(i).contentDescription
            if (childContentDescription != null) {
                return childContentDescription.toString()
            }
        }
    }
    return null
}
