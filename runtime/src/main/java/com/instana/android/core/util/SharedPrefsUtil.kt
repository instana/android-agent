/*
 * IBM Confidential
 * Copyright IBM Corp. 2023
 */

package com.instana.android.core.util

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.core.content.edit

/**
 * Utility class for simplified interaction with SharedPreferences.
 *
 * This class provides a convenient interface to read and write key-value pairs in the SharedPreferences
 * storage. It encapsulates the operations needed to access and modify SharedPreferences data, making
 * it easier and more readable to manage shared preferences across different parts of the application.
 *
 * Enhancements
 * - Possible to make it injectable with Hilt(not opting for now considering library size)
 *
 * Usage:
 * - Use the `getString` and `putString` methods to read and write string values.
 * - Use the `getLong` and `putLong` methods to read and write long integer values.
 *
 * Example:
 * ```
 * val username = SharedPrefsUtil.getString(context, value, defaultValue)
 * SharedPrefsUtil.putString(context, "username", "JohnDoe")
 * ```
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object SharedPrefsUtil {

    private const val PREFS_NAME = "instanaPrefs"

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves a string value from SharedPreferences.
     */
    fun getString(context: Context, key: String, defaultValue: String? = null) = getSharedPreferences(context).getString(key, defaultValue)

    /**
     * Stores a string value in SharedPreferences.
     */
    fun putString(context: Context, key: String, value: String) {
            getSharedPreferences(context).edit {
            putString(key, value)
        }
    }

    /**
     * Retrieves a long value from SharedPreferences.
     */
    fun getLong(context: Context, key: String, defaultValue: Long = 0) = getSharedPreferences(context).getLong(key, defaultValue)

    /**
     * Stores a long value in SharedPreferences.
     */
    fun putLong(context: Context, key: String, value: Long) {
            getSharedPreferences(context).edit {
            putLong(key, value)
        }
    }
}
