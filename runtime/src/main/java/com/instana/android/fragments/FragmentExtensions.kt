/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.fragments

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlin.math.min

internal fun Fragment.getLocalPathName(): String {
    return this.javaClass.`package`?.name.toString()+"."+this.javaClass.simpleName
}

internal fun Fragment.findContentDescription(): String? {
    if (this.tag!=null){
        return this.tag
    }
    if (view?.contentDescription != null) {
        return view?.contentDescription.toString()
    }
    if (view is ViewGroup) {
        for (i in 0 until min(2, (view as ViewGroup).childCount)) {
            val childContentDescription = (view as ViewGroup).getChildAt(i).contentDescription
            if (childContentDescription != null) {
                return childContentDescription.toString()
            }
        }
    }

    return null
}