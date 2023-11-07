/*
 * IBM Confidential
 * Copyright IBM Corp. 2023
 */

package com.instana.android.core.util

import android.content.Context
import androidx.annotation.RestrictTo
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A manager class for generating and managing a unique ID using SharedPreferences.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal object UniqueIdManager {
    private const val KEY_UNIQUE_ID = "usi_uniqueId"
    private const val KEY_TIMESTAMP = "usi_timestamp"

    private var uniqueId: String = ""
    private var timestamp: Long = 0

    /**
     * Initializes the UniqueIdManager with the provided application context.
     * Retrieves the unique ID from SharedPreferences if available and checks the timestamp.
     * If the timestamp is within the specified time limit, returns the same ID, otherwise generates and saves a new one.
     *
     * @param context The application context to initialize the manager.
     * @param usiRefreshTimeIntervalInHrs The time limit in hours for which the same ID will be returned.
     */
    fun initialize(context: Context, usiRefreshTimeIntervalInHrs: Long) {
        // Retrieve unique ID and timestamp from SharedPreferences
        uniqueId = SharedPrefsUtil.getString(context, KEY_UNIQUE_ID) ?: generateAndSaveUniqueId(context)
        timestamp = SharedPrefsUtil.getLong(context, KEY_TIMESTAMP)

        // Check the refresh time interval
        when {
            usiRefreshTimeIntervalInHrs < 0L -> {
                // No action required; retain the current uniqueId until the app is re-installed in the device.
            }
            usiRefreshTimeIntervalInHrs > 0L && hasTimeElapsed(usiRefreshTimeIntervalInHrs) -> {
                // Generate and save a new unique ID if time exceeds the limit
                uniqueId = generateAndSaveUniqueId(context)
            }
            usiRefreshTimeIntervalInHrs == 0L -> {
                // Will never track the device as a unique one
                uniqueId = ""
            }
        }

    }

    /**
     * Retrieves the unique ID.
     *
     * @return The unique ID if initialized, otherwise throws UninitializedPropertyAccessException.
     */
    fun getUniqueId(): String = uniqueId

    /**
     * Generates a new unique ID using UUID, saves it to SharedPreferences along with the current timestamp,
     * and returns the generated ID.
     *
     * @param context The application context for accessing SharedPreferences.
     * @return The generated unique ID.
     */
    private fun generateAndSaveUniqueId(context: Context): String = UUID.randomUUID().toString().also {
        uniqueId = it
        timestamp = System.currentTimeMillis()

        // Save the new unique ID and timestamp in SharedPreferences
        SharedPrefsUtil.putString(context, KEY_UNIQUE_ID, uniqueId)
        SharedPrefsUtil.putLong(context, KEY_TIMESTAMP, timestamp)
    }

    /**
     * Checks if the time elapsed is greater than the specified interval.
     *
     * @param intervalInHrs The time interval in hours.
     * @return True if the time has elapsed, false otherwise.
     */
    private fun hasTimeElapsed(intervalInHrs: Long) = System.currentTimeMillis() - timestamp > TimeUnit.HOURS.toMillis(intervalInHrs)

    /**
     * A hex encoded 64 bit random ID.
     */
    fun generateUniqueIdImpl(): String {
        val validIdCharacters = "0123456789abcdef"
        return buildString {
            repeat(16) {
                append(validIdCharacters.random())
            }
        }
    }

}
