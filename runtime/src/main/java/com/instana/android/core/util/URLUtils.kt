package com.instana.android.core.util

import com.instana.android.Instana

class URLUtils {
    companion object {
        private val queryRegex = "^[^?#]+\\?([^#]+)".toRegex()

        /**
         * If matches are found in query parameter keys, the value of those matches is set to `replacement`
         */
        fun redactURLQueryParams(url: String, replacement: String, regex: List<Regex>): String {
            val originalQuery = queryRegex.find(url)?.groupValues?.get(1) ?: return url

            val redactedParams = originalQuery.split("&").map { param ->
                val paramParts = param.split("=")
                val paramName = paramParts.elementAtOrNull(0)
                val paramValue = paramParts.elementAtOrNull(1)

                when {
                    paramName == null || paramValue == null -> param
                    regex.any { reg -> reg.find(paramName) != null } -> "$paramName=$replacement"
                    else -> "$paramName=$paramValue"
                }
            }
            val redactedQuery = redactedParams.joinToString("&")

            return url.replace(originalQuery, redactedQuery)
        }

        /**
         * If none of the regex patterns match the URL, remove the query parameters and fragment.
         */
        fun removeQueryParamsIfNotMatched(url: String): String {
            val queryTrackedDomainPatterns = Instana.queryTrackedDomainList
            synchronized(queryTrackedDomainPatterns) {
                // Using patterns: being interoperable with Java
                val regexList = queryTrackedDomainPatterns.map { it.toRegex() }

                // Check if any of the regex patterns match the URL
                if (regexList.any { regex -> regex.containsMatchIn(url) }) {
                    return url // Return the URL unchanged if any regex matches
                }

                // If no regex matches, remove query parameters and fragments
                return url.substringBefore("?").substringBefore("#")
            }
        }

    }
}
