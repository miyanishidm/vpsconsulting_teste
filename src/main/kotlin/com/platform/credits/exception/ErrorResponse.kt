package com.platform.credits.exception

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    @JsonProperty("path")
    val path: String,
    @JsonProperty("error_code")
    val errorCode: String? = null,
    val details: Map<String, Any>? = null
)
