/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

internal class RateLimiter(
    private val maxPerTenMinutes: Int,
    private val maxPerTenSeconds: Int
) {
    private var lastTenMinuteCount = 0
    private var lastTenSecondsCount = 0
    private var lastTenMinuteTimestamp = System.currentTimeMillis()
    private var lastTenSecondsTimestamp = System.currentTimeMillis()

    @Synchronized
    fun isRateExceeded(newItems: Int): Boolean {
        val now = System.currentTimeMillis()
        if (lastTenMinuteTimestamp + 10 * 60 * 1000 < now) {
            lastTenMinuteTimestamp = now
            lastTenMinuteCount = 0
        }
        if (lastTenSecondsTimestamp + 10 * 1000 < now) {
            lastTenSecondsTimestamp = now
            lastTenSecondsCount = 0
        }
        lastTenMinuteCount += newItems
        lastTenSecondsCount += newItems

        return lastTenMinuteCount > maxPerTenMinutes || lastTenSecondsCount > maxPerTenSeconds
    }
}
