/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.instana.android.core.InstanaLifeCycle
import com.instana.android.core.InstanaWorkManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(minSdk = 31, maxSdk = 31)
abstract class BaseTest {
    internal val app = ApplicationProvider.getApplicationContext<Application>()
    internal val mockWorkManager = mock<InstanaWorkManager>()
    internal val mockInstanaLifeCycle = mock<InstanaLifeCycle> {
        on { activityName } doReturn "activity"
    }
}