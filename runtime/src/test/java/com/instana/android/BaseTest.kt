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
@Config(minSdk = 16, maxSdk = 28)
abstract class BaseTest {
    internal val app = ApplicationProvider.getApplicationContext<Application>()
    internal val mockWorkManager = mock<InstanaWorkManager>()
    internal val mockInstanaLifeCycle = mock<InstanaLifeCycle> {
        on { activityName } doReturn "activity"
    }
}