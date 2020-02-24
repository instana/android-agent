/*
 * Created by Mikel Pascual (mikel@4rtstudio.com).
 */
package com.instana.android.core.event.models

@Suppress("unused")
enum class EffectiveConnectionType(val internalType: String) {
    TYPE_2G("2g"),
    TYPE_3G("3g"),
    TYPE_4G("4g"),
    TYPE_SLOW_2G("slow-2g");
}
