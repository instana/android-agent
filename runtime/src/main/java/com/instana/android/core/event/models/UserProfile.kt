/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.models

data class UserProfile(
    var userId: String? = null,
    var userName: String? = null,
    var userEmail: String? = null
)
