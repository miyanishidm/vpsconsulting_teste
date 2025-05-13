package com.platform.credits.dto.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TransactionResponse(
    @JsonProperty("transaction_id")
    val transactionId: UUID,

    @JsonProperty("partner_id")
    val partnerId: String,

    val type: TransactionType,

    val amount: BigDecimal,

    val description: String,

    val status: TransactionStatus,

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,

    @JsonProperty("completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val completedAt: LocalDateTime?
)
