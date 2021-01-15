package com.instana.flutter.flutter_agent

/**
 * Error codes Instana Flutter Agent could return
 *
 **/
enum class ErrorCode(val serialized: String) {
    MISSING_OR_INVALID_ARGUMENT("missingOrInvalidArg"),
    NOT_SETUP("instanaNotSetup"),
    LIST_FULL("listFull"),
}
