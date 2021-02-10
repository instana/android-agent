/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.event.worker

import android.content.pm.ProviderInfo
import com.instana.android.BaseTest
import org.junit.Assert.assertSame
import org.junit.Test

class EventWorkManagerInitializerShould : BaseTest() {

    @Test
    fun checkProviderContext() {
        val provider = EventWorkManagerInitializer()
        assertSame(null, provider.context)
        val info = ProviderInfo()
        provider.attachInfo(app, info)
        assertSame(app, provider.context)
    }

    @Test(expected = IllegalStateException::class)
    fun attachInfoIfAuthoritiesIsTheSameAsLibrary() {
        val provider = EventWorkManagerInitializer()
        val info = ProviderInfo()
        info.authority = "com.instana.android.core.instana-work-init"
        provider.attachInfo(app, info)
    }

    @Test(expected = NullPointerException::class)
    fun attachInfoWhenInfoNull() {
        val provider = EventWorkManagerInitializer()
        provider.attachInfo(null, null)
    }
}