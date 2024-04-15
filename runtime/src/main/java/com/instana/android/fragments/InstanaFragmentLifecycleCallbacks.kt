/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.instana.android.Instana
import com.instana.android.core.util.Logger
import com.instana.android.view.VisibleScreenNameTracker


internal class FragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

    private val fragmentStack = arrayListOf<Fragment>()
    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        if(Instana.config?.autoCaptureScreenNames==false) return
        var newNavController:NavController? = null
        try {
            newNavController = f.findNavController()
        }catch (e:IllegalStateException){
            Logger.i("Fragment not having navController")
        }catch (e:NullPointerException){
            Logger.i("Fragment init without navController")
        }
        val simpleFragmentClassName = f.javaClass.simpleName
        Logger.i("Fragment Resumed: $simpleFragmentClassName")
        if (f is NavHostFragment) {
            return
        }
        val (fragmentHierarchyType,fragmentNames) = findFragmentsInParallel(currentFragment = f)
        var label: String? = null
        /** This helps to identify the inner views of the fragments in a single nav controller*/
        if (newNavController != null) {
            label = newNavController.currentDestination?.label?.let {
                if (it != simpleFragmentClassName) "$it : $simpleFragmentClassName" else simpleFragmentClassName
            } ?: simpleFragmentClassName
        }
        VisibleScreenNameTracker.updateActivityFragmentViewData(
            VisibleScreenNameTracker.activityFragmentViewData.get()?.copy(
                fragmentLocalPathName = f.getLocalPathName(),
                fragmentClassName = simpleFragmentClassName,
                customFragmentScreenName = label ?: f.findContentDescription() ?: simpleFragmentClassName,
                activeFragmentList = fragmentNames,
                fragmentHierarchyType = fragmentHierarchyType.toString()
            )
        )
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        fragmentStack.remove(f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        fragmentStack.remove(f)
    }

    private fun findFragmentsInParallel(currentFragment: Fragment):Pair<FragmentHierarchyType,String>{
        val parallelFragmentList = mutableListOf<String>()
        var fragmentHierarchyType = FragmentHierarchyType.SINGLE
        fragmentStack.forEach {
            if(it.userVisibleHint){
                parallelFragmentList.add(it.javaClass.simpleName)
                fragmentHierarchyType = checkForHierarchy(currentFragment,it)
            }
        }
        parallelFragmentList.add(currentFragment.javaClass.simpleName)
        if(fragmentStack.contains(currentFragment).not()){
            fragmentStack.add(currentFragment)
        }
        return Pair(fragmentHierarchyType,parallelFragmentList.toString())
    }

    private fun checkForHierarchy(currentFragment: Fragment,existingFragment:Fragment):FragmentHierarchyType{
        return if(currentFragment.parentFragment is NavHostFragment && currentFragment.parentFragment?.parentFragment == existingFragment){
            FragmentHierarchyType.INNER_CHILD
        }else if(currentFragment.parentFragment == existingFragment){
            FragmentHierarchyType.INNER_CHILD
        }else{
            FragmentHierarchyType.PARALLEL
        }
    }

}