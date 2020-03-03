package com.instana.mobileeum

import android.os.AsyncTask

class FetchRestData : AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg params: Void): Boolean? =
        HttpUrlConnectionRequests.doGet(enableManual = false)
}