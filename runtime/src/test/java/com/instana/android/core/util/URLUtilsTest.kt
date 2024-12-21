package com.instana.android.core.util

import com.instana.android.Instana
import com.instana.android.core.util.URLUtils.Companion.removeQueryParamsIfNotMatched
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class URLUtilsTest {

    private val replacement = "<redacted>"
    private val regex = listOf(
        "password".toRegex(RegexOption.IGNORE_CASE),
        "key".toRegex(RegexOption.IGNORE_CASE),
        "^secret$".toRegex(RegexOption.IGNORE_CASE),
        "^hidden[0-9]$".toRegex(RegexOption.IGNORE_CASE),
    )

    @Before
    fun setup(){
        // Reset the tracked domain list before each test
        Instana.queryTrackedDomainList.clear()
    }


    @Test
    fun `test URL matches regex pattern`() {
        // Add regex pattern to track URLs with "10.0.2.2:8081"
        Instana.queryTrackedDomainList.add(".*10\\.0\\.2\\.2:8081.*".toRegex().toPattern())

        // URL that matches the pattern
        val url = "http://10.0.2.2:8081/images/women_cloth_06.jpeg?something=sdsd#sadasd"
        val result = removeQueryParamsIfNotMatched(url)

        // The URL should remain unchanged since it matches the regex
        assertEquals(url, result)
    }

    @Test
    fun `test URL does not match any regex pattern and query params are removed`() {
        // Add regex pattern to track URLs with "10.0.2.2:8081"
        Instana.queryTrackedDomainList.add(".*10\\.0\\.2\\.2:8081.*".toRegex().toPattern())

        // URL that does NOT match the pattern
        val url = "http://example.com/images/women_cloth_06.jpeg?something=sdsd#sadasd"
        val result = removeQueryParamsIfNotMatched(url)

        // The URL should have query parameters and fragment removed
        assertEquals("http://example.com/images/women_cloth_06.jpeg", result)
    }

    @Test
    fun `test URL without query params or fragment should remain unchanged`() {
        // URL without any query parameters or fragments
        val url = "http://example.com/images/women_cloth_06.jpeg"
        val result = removeQueryParamsIfNotMatched(url)

        // The URL should remain unchanged
        assertEquals(url, result)
    }

    @Test
    fun `test URL with only query params should have query removed`() {
        // Add regex pattern to track URLs with "10.0.2.2:8081"
        Instana.queryTrackedDomainList.add(".*10\\.0\\.2\\.2:8081.*".toRegex().toPattern())

        // URL with query parameters but not matching the pattern
        val url = "http://example.com/images/women_cloth_06.jpeg?param=value"
        val result = removeQueryParamsIfNotMatched(url)

        // The query parameters should be removed
        assertEquals("http://example.com/images/women_cloth_06.jpeg", result)
    }

    @Test
    fun `test URL with only fragment should have fragment removed`() {
        // Add regex pattern to track URLs with "10.0.2.2:8081"
        Instana.queryTrackedDomainList.add(".*10\\.0\\.2\\.2:8081.*".toRegex().toPattern())

        // URL with a fragment but not matching the pattern
        val url = "http://example.com/images/women_cloth_06.jpeg#section"
        val result = removeQueryParamsIfNotMatched(url)

        // The fragment should be removed
        assertEquals("http://example.com/images/women_cloth_06.jpeg", result)
    }

    @Test
    fun `test URL with both query and fragment should have both removed`() {
        // Add regex pattern to track URLs with "10.0.2.2:8081"
        Instana.queryTrackedDomainList.add(".*10\\.0\\.2\\.2:8081.*".toRegex().toPattern())

        // URL with query and fragment, and does not match the pattern
        val url = "http://example.com/images/women_cloth_06.jpeg?param=value#section"
        val result = removeQueryParamsIfNotMatched(url)

        // Both query parameters and fragment should be removed
        assertEquals("http://example.com/images/women_cloth_06.jpeg", result)
    }

    @Test
    fun `test edge case with empty URL`() {
        // Edge case: empty URL string
        val url = ""
        val result = removeQueryParamsIfNotMatched(url)

        // Should return empty string, no query params or fragment to remove
        assertEquals(url, result)
    }

    @Test
    fun `test URL with special characters and no query params`() {
        // URL with special characters but no query or fragment
        val url = "http://example.com/path/to/!@#$.jpeg"
        val result = removeQueryParamsIfNotMatched(url)
        val expected = "http://example.com/path/to/!@"
        // Should remain unchanged
        assertEquals(expected, result)
    }

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
