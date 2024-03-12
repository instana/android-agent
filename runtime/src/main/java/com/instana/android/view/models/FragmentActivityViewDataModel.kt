package com.instana.android.view.models

data class FragmentActivityViewDataModel(
    val activityClassName:String?=null,
    val activityLocalPathName:String?=null,
    val customActivityScreenName:String?=null,
    val fragmentClassName:String?=null,
    val fragmentLocalPathName:String?=null,
    val customFragmentScreenName:String?=null,
    val activeFragmentList:String?=null,
    val fragmentHierarchyType:String?=null
)
