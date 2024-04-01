/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.view

import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.view.models.FragmentActivityViewDataModel
import org.junit.Assert
import org.junit.Test
import org.mockito.internal.matchers.Null

class VisibleScreenNameTrackerTest :BaseTest(){


    @Test
    fun `test updateActivityFragmentViewData with isFromOrientationChange true`(){
        val fragmentActivityViewDataModel = FragmentActivityViewDataModel(
            activityClassName = "Activity",
            activityLocalPathName = "com.test.Activity",
            customActivityScreenName = "My Activity",
            fragmentClassName = "FragmentTest",
            fragmentLocalPathName = "com.test.FragmentTest",
            customFragmentScreenName = "FragmentTest",
            activeFragmentList = "[FragmentTest12]",
            fragmentHierarchyType = null

        )
        val fragmentActivityViewDataModel2 = FragmentActivityViewDataModel(
            activityClassName = null,
            activityLocalPathName = "com.test.Activity2",
            customActivityScreenName = "My Activity2",
            fragmentClassName = "FragmentTest2",
            fragmentLocalPathName = "com.test.FragmentTest2",
            customFragmentScreenName = "FragmentTest2",
            activeFragmentList = "[FragmentTest1]",
            fragmentHierarchyType = null

        )
        val visibleScreenNameTracker =VisibleScreenNameTracker
        val oldActiveFragmentList2 = getPrivateFieldValue(visibleScreenNameTracker,"oldActiveFragmentList")as String?
        VisibleScreenNameTracker.activityFragmentViewData.set(fragmentActivityViewDataModel2)
        VisibleScreenNameTracker.updateActivityFragmentViewData(fragmentActivityViewDataModel)
        val oldActiveFragmentList = getPrivateFieldValue(visibleScreenNameTracker,"oldActiveFragmentList") as String?
        Assert.assertEquals(oldActiveFragmentList,"[FragmentTest1]")
        VisibleScreenNameTracker.activityFragmentViewData.set(FragmentActivityViewDataModel())
    }

    @Test
    fun `test updateActivityFragmentViewData with isFromOrientationChange true with fragment list active `(){
        VisibleScreenNameTracker.activityFragmentViewData.set(FragmentActivityViewDataModel())
        val fragmentActivityViewDataModel = FragmentActivityViewDataModel(
            activityClassName = "Activity",
            activityLocalPathName = "com.test.Activity",
            customActivityScreenName = "My Activity",
            fragmentClassName = "FragmentTest",
            fragmentLocalPathName = "com.test.FragmentTest",
            customFragmentScreenName = "FragmentTest",
            activeFragmentList = "[FragmentTest12]",
            fragmentHierarchyType = null

        )
        val fragmentActivityViewDataModel2 = FragmentActivityViewDataModel(
            activityClassName = "Activity",
            activityLocalPathName = "com.test.Activity2",
            customActivityScreenName = "My Activity2",
            fragmentClassName = "FragmentTest12",
            fragmentLocalPathName = "com.test.FragmentTest2",
            customFragmentScreenName = "FragmentTest2",
            activeFragmentList = "[FragmentTest1]",
            fragmentHierarchyType = null

        )
        val visibleScreenNameTracker =VisibleScreenNameTracker
        val oldActiveFragmentList2 = getPrivateFieldValue(visibleScreenNameTracker,"oldActiveFragmentList")as String?
        VisibleScreenNameTracker.activityFragmentViewData.set(fragmentActivityViewDataModel2)
        VisibleScreenNameTracker.updateActivityFragmentViewData(fragmentActivityViewDataModel)
        val oldActiveFragmentList = getPrivateFieldValue(visibleScreenNameTracker,"oldActiveFragmentList") as String?
        Assert.assertEquals(oldActiveFragmentList,"[FragmentTest1]")
        VisibleScreenNameTracker.activityFragmentViewData.set(FragmentActivityViewDataModel())
    }

    @Test
    fun `test updating with null values targeting updateInitialViewMap`(){
        Instana.viewMeta.clear()
        val fragmentActivityViewDataModel = FragmentActivityViewDataModel(
            activityClassName = null,
            activityLocalPathName = null,
            customActivityScreenName = null,
            fragmentClassName = null,
            fragmentLocalPathName = null,
            customFragmentScreenName = null,
            activeFragmentList = null,
            fragmentHierarchyType = null
        )
        VisibleScreenNameTracker.updateActivityFragmentViewData(fragmentActivityViewDataModel)
        Assert.assertEquals(Instana.viewMeta.get(ScreenAttributes.ACTIVITY_CLASS_NAME.value).toString(),fragmentActivityViewDataModel.activityClassName.toString())
        VisibleScreenNameTracker.activityFragmentViewData.set(FragmentActivityViewDataModel())
    }

    @Test
    fun `test updating with null values targeting updateMetaForActivity`(){
        Instana.viewMeta.clear()
        val fragmentActivityViewDataModel = FragmentActivityViewDataModel(
            activityClassName = null,
            activityLocalPathName = null,
            customActivityScreenName = null,
            fragmentClassName = "test",
            fragmentLocalPathName = null,
            customFragmentScreenName = null,
            activeFragmentList = null,
            fragmentHierarchyType = null
        )
        val fragmentActivityViewDataModel2 = FragmentActivityViewDataModel(
            activityClassName = "test",
            activityLocalPathName = null,
            customActivityScreenName = null,
            fragmentClassName = null,
            fragmentLocalPathName = null,
            customFragmentScreenName = null,
            activeFragmentList = null,
            fragmentHierarchyType = null
        )
        VisibleScreenNameTracker.activityFragmentViewData.set(fragmentActivityViewDataModel2)
        VisibleScreenNameTracker.updateActivityFragmentViewData(fragmentActivityViewDataModel)
        Assert.assertEquals(Instana.viewMeta.get(ScreenAttributes.ACTIVITY_CLASS_NAME.value).toString(),fragmentActivityViewDataModel.activityClassName.toString())
        VisibleScreenNameTracker.activityFragmentViewData.set(FragmentActivityViewDataModel())
    }
    
    
    @Test
    fun `test updateInitialViewMap with newvalue object as null`(){

        val visibleScreenNameTracker = VisibleScreenNameTracker
        val updateInitialViewMapMethod = invokePrivateMethod2(visibleScreenNameTracker,"updateInitialViewMap",null,FragmentActivityViewDataModel::class.java)
        Assert.assertEquals(VisibleScreenNameTracker.initialViewMap[ScreenAttributes.ACTIVITY_SCREEN_NAME.value], "null")
    }

    @Test
    fun `test updateMetaForActivity with newvalue object as null`(){
        Instana.viewMeta.clear()
        val visibleScreenNameTracker = VisibleScreenNameTracker
        val updateInitialViewMapMethod = invokePrivateMethod2(visibleScreenNameTracker,"updateMetaForActivity",null,FragmentActivityViewDataModel::class.java)
        Instana.viewMeta.getAll().forEach{
            println("${it.key}:  ${it.value}")
        }
        Assert.assertEquals(Instana.viewMeta.getAll().size, 4)
    }

    @Test
    fun `test updateMetaForFragment with newvalue object as null`(){
        Instana.viewMeta.clear()
        val visibleScreenNameTracker = VisibleScreenNameTracker
        val updateInitialViewMapMethod = invokePrivateMethod2(visibleScreenNameTracker,"updateMetaForFragment",null,FragmentActivityViewDataModel::class.java)
        Instana.viewMeta.getAll().forEach{
            println("${it.key}:  ${it.value}")
        }
        Assert.assertEquals(Instana.viewMeta.getAll().size, 4)
    }

    @Test
    fun `test updateMetaForFragment with activeFragmentList as blank`(){
        Instana.viewMeta.clear()
        val visibleScreenNameTracker = VisibleScreenNameTracker
        val updateInitialViewMapMethod = invokePrivateMethod2(visibleScreenNameTracker,"updateMetaForFragment",FragmentActivityViewDataModel(activeFragmentList = ""),FragmentActivityViewDataModel::class.java)
        Instana.viewMeta.getAll().forEach{
            println("${it.key}:  ${it.value}")
        }
        Assert.assertEquals(Instana.viewMeta.getAll().size, 4)
    }

    @Test
    fun `test updateActivityFragmentViewData with object null`(){
        Instana.viewMeta.clear()
        VisibleScreenNameTracker.updateActivityFragmentViewData(null)
        Assert.assertEquals(Instana.viewMeta.getAll().size, 4)
    }
    
    @Test
    fun `test updateActivityFragmentViewData with object null and oldActivityClassName not null`(){
        VisibleScreenNameTracker.activityFragmentViewData.set(
            FragmentActivityViewDataModel(
                activityClassName = "activityClassName",
                activityLocalPathName = null,
                customActivityScreenName = null,
                fragmentClassName = null,
                fragmentLocalPathName = null,
                customFragmentScreenName = null,
                activeFragmentList = null,
                fragmentHierarchyType = null
            )
        )
        Instana.viewMeta.clear()
        VisibleScreenNameTracker.updateActivityFragmentViewData(null)
        Assert.assertEquals(Instana.viewMeta.getAll().size, 4)
    }

    @Test
    fun `test removeFragmentSection should remove the fragment details from the viewMeta`(){
        Instana.viewMeta.clear()
        Instana.viewMeta.apply {
            put(ScreenAttributes.FRAGMENT_RESUME_TIME.value, System.nanoTime().toString())
            put(ScreenAttributes.FRAGMENT_CLASS_NAME.value, "fragmentClassName")
            put(ScreenAttributes.FRAGMENT_LOCAL_PATH_NAME.value, "fragmentLocalPathName")
            put(ScreenAttributes.FRAGMENT_SCREEN_NAME.value, "customFragmentScreenName")
        }
        val visibleScreenNameTracker = VisibleScreenNameTracker
        invokePrivateMethod(visibleScreenNameTracker,"removeFragmentSection")
        Assert.assertEquals(Instana.viewMeta.getAll().size,0)
    }





}