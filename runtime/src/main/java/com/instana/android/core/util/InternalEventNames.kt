/*
 * IBM Confidential
 * PID 5737-N85, 5900-AG5
 * Copyright IBM Corp. 2024, 2024
 */

package com.instana.android.core.util

enum class InternalEventNames(val eventName: String,val titleName:String) {
    BEACON_DROP("beacon-drop","INSTANA_DROPPED_BEACON_SAMPLE"),
    FRAME_DROP("frame-drop","FrameDip"),
    ANR("anr","ANR"),
    LOW_MEMORY("low-memory","LowMemory");
    companion object {
        fun getEventNameForTitle(title: String): String {
            return values().find { it.titleName.contains(title, ignoreCase = true) }?.eventName ?: ""
        }
    }

}