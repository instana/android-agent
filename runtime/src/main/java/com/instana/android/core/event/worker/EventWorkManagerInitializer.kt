package com.instana.android.core.event.worker

import android.content.Context
import android.content.pm.ProviderInfo
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Initialized from manifest file, used to manually initialize WorkManager
 */
class EventWorkManagerInitializer : EventWorkerAbstractContentProvider() {

    override fun onCreate(): Boolean {
        context?.run {
            WorkManager.initialize(this, Configuration.Builder().build())
        }
        return true
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        if (info == null) {
            throw NullPointerException("EventWorkManagerInitializer ProviderInfo cannot be null.")
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if ("com.instana.android.core.instana-work-init" == info.authority) {
            throw IllegalStateException("Incorrect provider authority in manifest. Most likely due to a missing applicationId variable in application\'s build.gradle.")
        }
        super.attachInfo(context, info)
    }
}