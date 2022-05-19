/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.mobileeum.ui.list

import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    val list = listOf("one", "two", "three", "four", "five", "six")
}
