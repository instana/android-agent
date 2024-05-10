/*
 * (c) Copyright IBM Corp. 2021
 * (c) Copyright Instana Inc. and contributors 2021
 */

package com.instana.android.core.util

internal class RateLimiter(
    private val maxPerFiveMinutes: Int,
    private val maxPerTenSeconds: Int
) {
    private var lastFiveMinuteCount = 0
    private var lastTenSecondsCount = 0
    private var lastFiveMinuteTimestamp = System.currentTimeMillis()
    private var lastTenSecondsTimestamp = System.currentTimeMillis()

    @Synchronized
    fun isRateExceeded(newItems: Int): Boolean {
        val now = System.currentTimeMillis()
        if (lastFiveMinuteTimestamp + 5 * 60 * 1000 < now) {
            lastFiveMinuteTimestamp = now
            lastFiveMinuteCount = 0
        }
        if (lastTenSecondsTimestamp + 10 * 1000 < now) {
            lastTenSecondsTimestamp = now
            lastTenSecondsCount = 0
        }
        lastFiveMinuteCount += newItems
        lastTenSecondsCount += newItems

        return lastFiveMinuteCount > maxPerFiveMinutes || lastTenSecondsCount > maxPerTenSeconds
    }
}
