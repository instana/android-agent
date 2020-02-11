package com.instana.mobileeum

import android.app.Application
import com.instana.android.Instana

class DemoAppKotlin : Application() {

    override fun onCreate() {
        super.onCreate()
        Instana.init(this)
    }
}