package com.platform.credits.exception

import java.time.LocalDateTime

data class ValidationErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val errors: List<FieldError>
)

data class FieldError(
    val field: String,
    val message: String
)
