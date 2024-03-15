package com.instana.android.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class URLUtilsTest {

    private val replacement = "<redacted>"
    private val regex = listOf(
        "password".toRegex(RegexOption.IGNORE_CASE),
        "key".toRegex(RegexOption.IGNORE_CASE),
        "^secret$".toRegex(RegexOption.IGNORE_CASE),
        "^hidden[0-9]$".toRegex(RegexOption.IGNORE_CASE),
    )

    @Test
    fun redactURLQueryParams_queryNotMatchingRegex() {
        assertEquals("https://example.com?param1=1&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?param1=1&param2=2", replacement, listOf("nonexistent".toRegex())))
    }

    @Test
    fun redactURLQueryParams_invalidURL() {
        assertEquals("not a url",
            URLUtils.redactURLQueryParams("not a url", replacement, regex))
        assertEquals("non-existent-tld.unknown",
            URLUtils.redactURLQueryParams("non-existent-tld.unknown", replacement, regex))
        assertEquals("unknown://strange-protocol.com",
            URLUtils.redactURLQueryParams("unknown://strange-protocol.com", replacement, regex))
    }

    @Test
    fun redactURLQueryParams_noQuery() {
        assertEquals("https://example.com",
            URLUtils.redactURLQueryParams("https://example.com", replacement, regex))
        assertEquals("https://example.com/",
            URLUtils.redactURLQueryParams("https://example.com/", replacement, regex))
        assertEquals("https://example.com?",
            URLUtils.redactURLQueryParams("https://example.com?", replacement, regex))
    }

    @Test
    fun redactURLQueryParams_queryWithAnchor() {
        assertEquals("https://example.com#anchor",
            URLUtils.redactURLQueryParams("https://example.com#anchor", replacement, regex))
        assertEquals("https://example.com?param1=1#anchor",
            URLUtils.redactURLQueryParams("https://example.com?param1=1#anchor", replacement, regex))
        assertEquals("https://example.com?param1=1&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?param1=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?password=$replacement#anchor",
            URLUtils.redactURLQueryParams("https://example.com?password=1#anchor", replacement, regex))
        assertEquals("https://example.com?password=$replacement&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?password=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?param1=1&password=$replacement#anchor",
            URLUtils.redactURLQueryParams("https://example.com?param1=1&password=2#anchor", replacement, regex))
        assertEquals("https://example.com?key=$replacement&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?key=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?secret=$replacement&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?secret=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?secret1=1&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?secret1=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?hidden1=$replacement&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?hidden1=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?hidden2=$replacement&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?hidden2=1&param2=2#anchor", replacement, regex))
    }

    @Test
    fun redactURLQueryParams_queryWithoutAnchor() {
        assertEquals("https://example.com",
            URLUtils.redactURLQueryParams("https://example.com", replacement, regex))
        assertEquals("https://example.com?param1=1",
            URLUtils.redactURLQueryParams("https://example.com?param1=1", replacement, regex))
        assertEquals("https://example.com?param1=1&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?param1=1&param2=2", replacement, regex))
        assertEquals("https://example.com?password=$replacement",
            URLUtils.redactURLQueryParams("https://example.com?password=1", replacement, regex))
        assertEquals("https://example.com?password=$replacement&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?password=1&param2=2", replacement, regex))
        assertEquals("https://example.com?param1=1&password=$replacement",
            URLUtils.redactURLQueryParams("https://example.com?param1=1&password=2", replacement, regex))
        assertEquals("https://example.com?key=$replacement&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?key=1&param2=2", replacement, regex))
        assertEquals("https://example.com?secret=$replacement&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?secret=1&param2=2", replacement, regex))
        assertEquals("https://example.com?secret1=1&param2=2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?secret1=1&param2=2#anchor", replacement, regex))
        assertEquals("https://example.com?hidden1=$replacement&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?hidden1=1&param2=2", replacement, regex))
        assertEquals("https://example.com?hidden2=$replacement&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?hidden2=1&param2=2", replacement, regex))
    }

    @Test
    fun redactURLQueryParams_parameterWithoutValue() {
        assertEquals("https://example.com",
            URLUtils.redactURLQueryParams("https://example.com", replacement, regex))
        assertEquals("https://example.com?param1",
            URLUtils.redactURLQueryParams("https://example.com?param1", replacement, regex))
        assertEquals("https://example.com?param1&param2=2",
            URLUtils.redactURLQueryParams("https://example.com?param1&param2=2", replacement, regex))
        assertEquals("https://example.com?password=$replacement",
            URLUtils.redactURLQueryParams("https://example.com?password=1", replacement, regex))
        assertEquals("https://example.com?password=$replacement&param2",
            URLUtils.redactURLQueryParams("https://example.com?password=1&param2", replacement, regex))
        assertEquals("https://example.com?param1&password=$replacement",
            URLUtils.redactURLQueryParams("https://example.com?param1&password=2", replacement, regex))
        assertEquals("https://example.com?key=$replacement&param2",
            URLUtils.redactURLQueryParams("https://example.com?key=1&param2", replacement, regex))
        assertEquals("https://example.com?secret=$replacement&param2",
            URLUtils.redactURLQueryParams("https://example.com?secret=1&param2", replacement, regex))
        assertEquals("https://example.com?secret1=1&param2#anchor",
            URLUtils.redactURLQueryParams("https://example.com?secret1=1&param2#anchor", replacement, regex))
        assertEquals("https://example.com?hidden1=$replacement&param2",
            URLUtils.redactURLQueryParams("https://example.com?hidden1=1&param2", replacement, regex))
        assertEquals("https://example.com?hidden2=$replacement&param2",
            URLUtils.redactURLQueryParams("https://example.com?hidden2=1&param2", replacement, regex))
    }
}
