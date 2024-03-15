/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.view

import com.instana.android.Instana
import com.instana.android.view.models.FragmentActivityViewDataModel
import java.util.concurrent.atomic.AtomicReference

object VisibleScreenNameTracker {

    internal var activityFragmentViewData: AtomicReference<FragmentActivityViewDataModel?> = AtomicReference(null)

    internal var initialViewMap: Map<String, String> = emptyMap()

    private var isFromOrientationChange: Boolean = false
    private var oldActiveFragmentList: String? = null

    /**
     * Currently keeping screen details in meta; can be moved to another beacon attribute in the future
     */
    internal fun updateActivityFragmentViewData(newValue: FragmentActivityViewDataModel?) {
        val oldActivityClassName = activityFragmentViewData.get()?.activityClassName
        //Logic to identify the orientation changes
        if (activityFragmentViewData.get()?.activeFragmentList != null && !isFromOrientationChange) {
            oldActiveFragmentList = activityFragmentViewData.get()?.activeFragmentList
        }
        activityFragmentViewData.set(newValue)

        //Initial view
        if (oldActivityClassName == null) {
            updateInitialViewMap(newValue)
        }else{
            if (oldActivityClassName != newValue?.activityClassName) {
                updateMetaForActivity(newValue)
                isFromOrientationChange = false
                oldActiveFragmentList = null
            }
            if (!newValue?.fragmentClassName.isNullOrBlank()) {
                if (oldActivityClassName == newValue?.activityClassName
                    && oldActiveFragmentList?.contains(newValue.fragmentClassName.toString()) == true) {
                    isFromOrientationChange = true
                    return
                }
                updateMetaForFragment(newValue)
            }
        }

    }

    private fun updateInitialViewMap(newValue: FragmentActivityViewDataModel?) {
        initialViewMap = mapOf(
            ScreenAttributes.ACTIVITY_CREATED_TIME.value to System.nanoTime().toString(),
            ScreenAttributes.ACTIVITY_CLASS_NAME.value to "${newValue?.activityClassName}",
            ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value to "${newValue?.activityLocalPathName}",
            ScreenAttributes.ACTIVITY_SCREEN_NAME.value to "${newValue?.customActivityScreenName}"
        )
        setViewName(newValue?.customActivityScreenName)
    }

    private fun updateMetaForActivity(newValue: FragmentActivityViewDataModel?) {
        Instana.viewMeta.apply {
            put(ScreenAttributes.ACTIVITY_CREATED_TIME.value, System.nanoTime().toString())
            put(ScreenAttributes.ACTIVITY_CLASS_NAME.value, "${newValue?.activityClassName}")
            put(ScreenAttributes.ACTIVITY_LOCAL_PATH_NAME.value, "${newValue?.activityLocalPathName}")
            put(ScreenAttributes.ACTIVITY_SCREEN_NAME.value, "${newValue?.customActivityScreenName}")
        }
        removeFragmentSection()
        setViewName(newValue?.customActivityScreenName)
    }

    private fun updateMetaForFragment(newValue: FragmentActivityViewDataModel?) {
        Instana.viewMeta.apply {
            put(ScreenAttributes.FRAGMENT_RESUME_TIME.value, System.nanoTime().toString())
            put(ScreenAttributes.FRAGMENT_CLASS_NAME.value, "${newValue?.fragmentClassName}")
            put(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value, "${newValue?.fragmentLocalPathName}")
            put(ScreenAttributes.FRAGMENT_SCREEN_NAME.value, "${newValue?.customFragmentScreenName}")
        }

        if (!newValue?.activeFragmentList.isNullOrBlank()) {
            Instana.viewMeta.put(ScreenAttributes.FRAGMENT_ACTIVE_SCREENS_LIST.value, "${newValue?.activeFragmentList}")
        }
        setViewName(newValue?.customFragmentScreenName)
    }

    private fun setViewName(viewName: String?) {
        Instana.view = viewName
    }
}

/**
 * This can be utilised for consuming SemanticAttributes from otel-android in future
 */
enum class ScreenAttributes(val value: String) {
    ACTIVITY_CLASS_NAME("act.class.name"),
    ACTIVITY_LOCAL_PATH_NAME("act.local.path.name"),
    ACTIVITY_SCREEN_NAME("act.screen.name"),
    ACTIVITY_CREATED_TIME("act.created.time"),
    FRAGMENT_CLASS_NAME("frag.class.name"),
    FRAGMENT_LOCAL_PATH_NAME("frag.local.path.name"),
    FRAGMENT_SCREEN_NAME("frag.screen.name"),
    FRAGMENT_ACTIVE_SCREENS_LIST("frag.active.screen.list"),
    FRAGMENT_RESUME_TIME("frag.resume.time"),
}

/**
 * Avoiding adding of the fragment details while moving to a new activity without fragments
 */
fun removeFragmentSection() {
    Instana.viewMeta.remove(ScreenAttributes.FRAGMENT_SCREEN_NAME.value)
    Instana.viewMeta.remove(ScreenAttributes.FRAGMENT_CLASS_NAME.value)
    Instana.viewMeta.remove(ScreenAttributes.FRAGMENT_ACTIVE_SCREENS_LIST.value)
    Instana.viewMeta.remove(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value)
}
