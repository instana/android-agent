package com.instana.android.crash

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR

/**
 * Persists crash to storage before app exits
 * Crash threads stacktrace is on disk so that work manager worker can read it upon
 * initialization (limit on size of the payload to work manager's work is 1mb)
 */
@SuppressLint("ApplySharedPref")
@RestrictTo(RestrictTo.Scope.LIBRARY)
object CrashEventStore {

    private const val PREF_NAME = "InstanaPriorityStore"
    private const val KEY_WORKER_TAG = "worker_tag"
    private const val KEY_WORKER_SERIALIZED = "worker_serialized"

    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    val tag: String
        get() {
            return pref?.getString(KEY_WORKER_TAG, EMPTY_STR) ?: EMPTY_STR
        }

    val serialized: String
        get() {
            return pref?.getString(KEY_WORKER_SERIALIZED, EMPTY_STR) ?: EMPTY_STR
        }

    fun init(context: Context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveEvent(tag: String, serialized: String) {
        editor = pref?.edit()
        editor?.apply {
            putString(KEY_WORKER_TAG, tag)
            putString(KEY_WORKER_SERIALIZED, serialized)
        }?.commit()
    }

    /**
     * testing purposes only
     */
    @VisibleForTesting
    fun reset() {
        clear()
        editor = null
        pref = null
    }

    /**
     * Clear session details
     */
    fun clear() {
        if (editor == null) {
            editor = pref?.edit()
        }
        editor?.apply { clear() }?.commit()
    }
}