package com.instana.android.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.RestrictTo
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import java.util.*

@SuppressLint("ApplySharedPref")
@RestrictTo(RestrictTo.Scope.LIBRARY)
object IdProvider {

    val sessionId: String = UUID.randomUUID().toString()

    fun eventId(): String = UUID.randomUUID().toString()

    private const val PREF_NAME = "InstanaAppId"
    private const val KEY_CLIENT_ID = "clientId"

    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    val clientId: String
        get() {
            var id = pref?.getString(KEY_CLIENT_ID, EMPTY_STR) ?: EMPTY_STR
            if (id == EMPTY_STR) {
                id = UUID.randomUUID().toString()
                editor = pref?.edit()
                editor?.putString(KEY_CLIENT_ID, id)?.commit()
            }
            return id
        }

    fun init(context: Context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}