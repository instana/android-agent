/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.fragments

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.instana.android.BaseTest
import com.instana.android.Instana
import com.instana.android.InstanaTest
import com.instana.android.activity.InstanaActivityLifecycleCallbacks
import com.instana.android.activity.findContentDescription
import com.instana.android.core.InstanaConfig
import com.instana.android.core.InstanaWorkManager
import com.instana.android.session.SessionService
import com.instana.android.view.VisibleScreenNameTracker
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

private class TestFragment1 : Fragment()
private class TestFragment2 : NavHostFragment()
private class TestActivity1 : Activity()

class FragmentLifecycleCallbacksTest : BaseTest() {

    @Mock
    private lateinit var mockFragmentManager: FragmentManager

    lateinit var config: InstanaConfig

    lateinit var wrkManager:InstanaWorkManager

    @Before
    fun setUp() {
        config = InstanaConfig(InstanaTest.API_KEY, InstanaTest.SERVER_URL, autoCaptureScreenNames = true)
        wrkManager = InstanaWorkManager(config, app)
        Instana.config = config
        MockitoAnnotations.openMocks(this)
        Instana.sessionId = null
        SessionService(app, mockWorkManager, config)
    }

    @Test
    fun `test onFragmentResumed should update VisibleScreenNameTracker for regular Fragment`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = TestFragment1()
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        Assert.assertEquals(VisibleScreenNameTracker.activityFragmentViewData.get()?.customFragmentScreenName , "TestFragment1")
        assert(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentLocalPathName == "com.instana.android.fragments.TestFragment1")
        assert(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentHierarchyType == "SINGLE")
        assert(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentClassName == "TestFragment1")
        assert(VisibleScreenNameTracker.activityFragmentViewData.get()?.activeFragmentList == "[TestFragment1]")
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
    }

    @Test
    fun `test onFragmentResumed should handle NavHostFragment without updating VisibleScreenNameTracker`() {
        val mockFragment = mock(NavHostFragment::class.java)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, mockFragment)
        assert(VisibleScreenNameTracker.activityFragmentViewData.get()?.customFragmentScreenName == null)
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
    }

    @Test
    fun `test onFragmentDestroyed should remove fragment from stack`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = TestFragment1()
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        val fragmentStack1 = getPrivateFieldValue(fragmentLifecycleCallbacks, "fragmentStack") as ArrayList<Fragment>
        assert(fragmentStack1.isNotEmpty())
        fragmentLifecycleCallbacks.onFragmentDestroyed(mockFragmentManager, testFragment)
        val fragmentStack = getPrivateFieldValue(fragmentLifecycleCallbacks, "fragmentStack") as ArrayList<Fragment>
        assert(fragmentStack.isEmpty())
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
    }

    @Test
    fun `test onFragmentPaused should remove fragment from stack`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = TestFragment1()
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        val fragmentStack1 = getPrivateFieldValue(fragmentLifecycleCallbacks, "fragmentStack") as ArrayList<Fragment>
        assert(fragmentStack1.isNotEmpty())
        fragmentLifecycleCallbacks.onFragmentPaused(mockFragmentManager, testFragment)
        val fragmentStack = getPrivateFieldValue(fragmentLifecycleCallbacks, "fragmentStack") as ArrayList<Fragment>
        assert(fragmentStack.isEmpty())
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
    }

    @Test
    fun `test onFragmentResumed should update VisibleScreenNameTracker for regular Fragment with userVisibleHint true`() {
        val mockActivity = mock(Activity::class.java)
        val mockView = mock(View::class.java)
        `when`(mockActivity.localClassName).thenReturn("MockActivity")
        `when`(mockActivity.findViewById<View>(android.R.id.content)).thenReturn(mockView)
        `when`(mockView.contentDescription).thenReturn("Mock Activity")
        `when`(mockActivity.findContentDescription()).thenReturn("MockActivityDescription")
        val testFragment = mock(Fragment::class.java)
        `when`(testFragment.userVisibleHint).thenReturn(true)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val instanaActivityLifecycleCallbacks = InstanaActivityLifecycleCallbacks()
        instanaActivityLifecycleCallbacks.onActivityResumed(mockActivity)
        fragmentLifecycleCallbacks.onFragmentResumed(mockFragmentManager, testFragment)
        Assert.assertEquals(VisibleScreenNameTracker.activityFragmentViewData.get()?.fragmentClassName , "Fragment")
        VisibleScreenNameTracker.activityFragmentViewData.set(null)
    }


    @Test
    fun `test checkForHierarchy condition 1 INNER_CHILD currentFragment - parentFragment is NavHostFragment`() {
        val mockCurrentFragment = mock(Fragment::class.java)
        val mockExistingFragment = mock(Fragment::class.java)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val checkForHierarchyMethod = FragmentLifecycleCallbacks::class.java.getDeclaredMethod(
            "checkForHierarchy",
            Fragment::class.java,
            Fragment::class.java
        )
        checkForHierarchyMethod.isAccessible = true
        `when`(mockCurrentFragment.parentFragment).thenReturn(NavHostFragment())
        `when`(mockCurrentFragment.parentFragment?.parentFragment).thenReturn(mockExistingFragment)
        val hierarchy = checkForHierarchyMethod.invoke(fragmentLifecycleCallbacks, mockCurrentFragment, mockExistingFragment)
        Assert.assertEquals(hierarchy, FragmentHierarchyType.INNER_CHILD)
    }

    @Test
    fun `test checkForHierarchy condition 2 INNER_CHILD currentFragment - direct parentFragment is NavHostFragment`() {
        val mockCurrentFragment = mock(Fragment::class.java)
        val mockExistingFragment = mock(Fragment::class.java)
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val checkForHierarchyMethod = FragmentLifecycleCallbacks::class.java.getDeclaredMethod(
            "checkForHierarchy",
            Fragment::class.java,
            Fragment::class.java
        )
        checkForHierarchyMethod.isAccessible = true
        `when`(mockCurrentFragment.parentFragment).thenReturn(NavHostFragment())
        `when`(mockCurrentFragment.parentFragment?.parentFragment).thenReturn(null)
        `when`(mockCurrentFragment.parentFragment).thenReturn(null)
        val hierarchy = checkForHierarchyMethod.invoke(fragmentLifecycleCallbacks, mockCurrentFragment, mockExistingFragment)
        Assert.assertEquals(hierarchy, FragmentHierarchyType.PARALLEL)
    }


    @Test
    fun `test findFragmentsInParallel check condition where return type will have hierarchy`() {
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val mockFragmentExisting = mock(Fragment::class.java)
        val mockFragmentExisting2 = mock(Fragment::class.java)
        val mockFragmentCurrent = mock(Fragment::class.java)
        val stackfragment = arrayListOf(mockFragmentExisting, mockFragmentExisting2)
        setPrivateField(fragmentLifecycleCallbacks, "fragmentStack", stackfragment)
        `when`(mockFragmentExisting2.userVisibleHint).thenReturn(true)
        `when`(mockFragmentExisting.userVisibleHint).thenReturn(false)
        `when`(mockFragmentCurrent.parentFragment).thenReturn(NavHostFragment())
        `when`(mockFragmentCurrent.parentFragment?.parentFragment).thenReturn(mockFragmentExisting2)
        val findFragmentsInParallelMethod = invokePrivateMethod2(
            fragmentLifecycleCallbacks,
            "findFragmentsInParallel",
            mockFragmentCurrent,
            Fragment::class.java
        ) as Pair<FragmentHierarchyType,String>
        Assert.assertEquals(findFragmentsInParallelMethod.first, FragmentHierarchyType.INNER_CHILD)
    }

    @Test
    fun `test findFragmentsInParallel check condition where return type will have hierarchy with Inner child`() {
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val mockFragmentExisting = mock(Fragment::class.java)
        val mockFragmentExisting2 = mock(Fragment::class.java)
        val mockFragmentCurrent = mock(Fragment::class.java)
        val navHostFragment = mock(NavHostFragment::class.java)
        val stackfragment = arrayListOf(mockFragmentExisting, mockFragmentExisting2)
        setPrivateField(fragmentLifecycleCallbacks, "fragmentStack", stackfragment)
        `when`(mockFragmentExisting2.userVisibleHint).thenReturn(true)
        `when`(mockFragmentExisting.userVisibleHint).thenReturn(true)
        `when`(mockFragmentCurrent.parentFragment).thenReturn(navHostFragment)
        `when`(mockFragmentCurrent.parentFragment?.parentFragment).thenReturn(mockFragmentExisting2)
        val findFragmentsInParallelMethod = invokePrivateMethod2(
            fragmentLifecycleCallbacks,
            "findFragmentsInParallel",
            mockFragmentCurrent,
            Fragment::class.java
        ) as Pair<FragmentHierarchyType,String>
        Assert.assertEquals(findFragmentsInParallelMethod.first, FragmentHierarchyType.INNER_CHILD)
    }

    @Test
    fun `test findFragmentsInParallel check condition where stack has the fragment already`() {
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val mockFragmentExisting = mock(Fragment::class.java)
        val mockFragmentExisting2 = mock(Fragment::class.java)
        val mockFragmentCurrent = mock(Fragment::class.java)
        val navHostFragment = mock(NavHostFragment::class.java)
        val stackfragment = arrayListOf(mockFragmentExisting, mockFragmentExisting2,mockFragmentCurrent)
        setPrivateField(fragmentLifecycleCallbacks, "fragmentStack", stackfragment)
        `when`(mockFragmentExisting2.userVisibleHint).thenReturn(true)
        `when`(mockFragmentExisting.userVisibleHint).thenReturn(true)
        `when`(mockFragmentCurrent.parentFragment).thenReturn(navHostFragment)
        `when`(mockFragmentCurrent.parentFragment?.parentFragment).thenReturn(mockFragmentExisting2)
        val findFragmentsInParallelMethod = invokePrivateMethod2(
            fragmentLifecycleCallbacks,
            "findFragmentsInParallel",
            mockFragmentCurrent,
            Fragment::class.java
        ) as Pair<FragmentHierarchyType,String>
        Assert.assertEquals(findFragmentsInParallelMethod.first, FragmentHierarchyType.INNER_CHILD)
    }

    @Test
    fun `test findFragmentsInParallel check condition where return type will have hierarchy with PARALLEL when super parent not exist`() {
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val mockFragmentExisting = mock(Fragment::class.java)
        val mockFragmentExisting2 = mock(Fragment::class.java)
        val mockFragmentCurrent = mock(Fragment::class.java)
        val navHostFragment = mock(NavHostFragment::class.java)
        val stackfragment = arrayListOf(mockFragmentExisting, mockFragmentExisting2)
        setPrivateField(fragmentLifecycleCallbacks, "fragmentStack", stackfragment)
        `when`(mockFragmentExisting2.userVisibleHint).thenReturn(true)
        `when`(mockFragmentExisting.userVisibleHint).thenReturn(true)
        `when`(mockFragmentCurrent.parentFragment).thenReturn(navHostFragment)
        `when`(mockFragmentCurrent.parentFragment?.parentFragment).thenReturn(navHostFragment)
        val findFragmentsInParallelMethod = invokePrivateMethod2(
            fragmentLifecycleCallbacks,
            "findFragmentsInParallel",
            mockFragmentCurrent,
            Fragment::class.java
        ) as Pair<FragmentHierarchyType,String>
        Assert.assertEquals(findFragmentsInParallelMethod.first, FragmentHierarchyType.PARALLEL)
    }

    @Test
    fun `test parent fragment not navhost fragemnt`() {
        val fragmentLifecycleCallbacks = FragmentLifecycleCallbacks()
        val mockFragmentExisting = mock(Fragment::class.java)
        val mockFragmentExisting2 = mock(Fragment::class.java)
        val mockFragmentCurrent = mock(Fragment::class.java)
        val navHostFragment = mock(NavHostFragment::class.java)
        val stackfragment = arrayListOf(mockFragmentExisting, mockFragmentExisting2)
        setPrivateField(fragmentLifecycleCallbacks, "fragmentStack", stackfragment)
        `when`(mockFragmentExisting2.userVisibleHint).thenReturn(true)
        `when`(mockFragmentExisting.userVisibleHint).thenReturn(true)
        `when`(mockFragmentCurrent.parentFragment).thenReturn(mockFragmentExisting2)
        `when`(mockFragmentCurrent.parentFragment?.parentFragment).thenReturn(navHostFragment)
        val findFragmentsInParallelMethod = invokePrivateMethod2(
            fragmentLifecycleCallbacks,
            "findFragmentsInParallel",
            mockFragmentCurrent,
            Fragment::class.java
        ) as Pair<FragmentHierarchyType,String>
        Assert.assertEquals(findFragmentsInParallelMethod.first, FragmentHierarchyType.INNER_CHILD)
    }

}
