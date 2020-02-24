package com.instana.android.crash

import com.instana.android.BaseTest
import com.instana.android.core.util.ConstantsAndUtil.EMPTY_STR
import junit.framework.Assert.assertEquals
import org.junit.Test

class CrashEventStoreShould : BaseTest() {

    @Test
    fun returnEmptyWhenNotInitialized() {
        CrashEventStore.reset()
        assertEquals(CrashEventStore.tag, EMPTY_STR)
    }

    @Test
    fun getTagWhenEmpty() {
        CrashEventStore.init(app)
        assertEquals(CrashEventStore.tag, EMPTY_STR)
    }

    @Test
    fun getTag() {
        CrashEventStore.init(app)
        CrashEventStore.saveEvent("tag", "json")
        assertEquals(CrashEventStore.tag, "tag")
    }

    @Test
    fun getJsonWhenEmpty() {
        CrashEventStore.init(app)
        CrashEventStore.reset()
        assertEquals(CrashEventStore.serialized, EMPTY_STR)
    }

    @Test
    fun getJson() {
        CrashEventStore.init(app)
        CrashEventStore.saveEvent("tag", "json")
        assertEquals(CrashEventStore.serialized, "json")
    }

    @Test
    fun saveEvent() {
        CrashEventStore.init(app)
        CrashEventStore.saveEvent("tag", "json")
        assertEquals(CrashEventStore.tag, "tag")
        assertEquals(CrashEventStore.serialized, "json")
    }

    @Test
    fun reset() {
        CrashEventStore.init(app)
        CrashEventStore.reset()
        assertEquals(CrashEventStore.tag, EMPTY_STR)
        assertEquals(CrashEventStore.serialized, EMPTY_STR)
    }

    @Test
    fun clear() {
        CrashEventStore.init(app)
        assertEquals(CrashEventStore.tag, EMPTY_STR)
        assertEquals(CrashEventStore.serialized, EMPTY_STR)
    }
}