package com.instana.android.instrumentation

enum class InstrumentationType(val type: Int = 0) {
    AUTO(1),
    MANUAL(2),
    DISABLED(3),
    ALL(0)
}