/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.view.models

import org.junit.Assert.assertEquals
import org.junit.Test

class FragmentActivityViewDataModelTest {

    @Test
    fun `createFragmentActivityViewDataModel should create a valid object with default values`() {
        val fragmentActivityViewDataModel = FragmentActivityViewDataModel()
        assertEquals(null, fragmentActivityViewDataModel.activityClassName)
        assertEquals(null, fragmentActivityViewDataModel.activityLocalPathName)
        assertEquals(null, fragmentActivityViewDataModel.customActivityScreenName)
        assertEquals(null, fragmentActivityViewDataModel.fragmentClassName)
        assertEquals(null, fragmentActivityViewDataModel.fragmentLocalPathName)
        assertEquals(null, fragmentActivityViewDataModel.customFragmentScreenName)
        assertEquals(null, fragmentActivityViewDataModel.activeFragmentList)
        assertEquals(null, fragmentActivityViewDataModel.fragmentHierarchyType)
    }

    @Test
    fun `createFragmentActivityViewDataModel should create a valid object with specified values`() {
        val activityClassName = "MainActivity"
        val activityLocalPathName = "com.example.MainActivity"
        val customActivityScreenName = "Main Screen"
        val fragmentClassName = "HomeFragment"
        val fragmentLocalPathName = "com.example.HomeFragment"
        val customFragmentScreenName = "Home Screen"
        val activeFragmentList = "[HomeFragment, DetailsFragment]"
        val fragmentHierarchyType = "Parallel"

        val fragmentActivityViewDataModel = FragmentActivityViewDataModel(
            activityClassName = activityClassName,
            activityLocalPathName = activityLocalPathName,
            customActivityScreenName = customActivityScreenName,
            fragmentClassName = fragmentClassName,
            fragmentLocalPathName = fragmentLocalPathName,
            customFragmentScreenName = customFragmentScreenName,
            activeFragmentList = activeFragmentList,
            fragmentHierarchyType = fragmentHierarchyType
        )

        assertEquals(activityClassName, fragmentActivityViewDataModel.activityClassName)
        assertEquals(activityLocalPathName, fragmentActivityViewDataModel.activityLocalPathName)
        assertEquals(customActivityScreenName, fragmentActivityViewDataModel.customActivityScreenName)
        assertEquals(fragmentClassName, fragmentActivityViewDataModel.fragmentClassName)
        assertEquals(fragmentLocalPathName, fragmentActivityViewDataModel.fragmentLocalPathName)
        assertEquals(customFragmentScreenName, fragmentActivityViewDataModel.customFragmentScreenName)
        assertEquals(activeFragmentList, fragmentActivityViewDataModel.activeFragmentList)
        assertEquals(fragmentHierarchyType, fragmentActivityViewDataModel.fragmentHierarchyType)
    }
}
