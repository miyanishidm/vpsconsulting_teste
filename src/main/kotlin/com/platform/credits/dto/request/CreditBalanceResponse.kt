package com.platform.credits.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreditBalanceResponse(
    @JsonProperty("partner_id")
    val partnerId: String,

    @JsonProperty("partner_name")
    val partnerName: String,

    @JsonProperty("credit_balance")
    val creditBalance: BigDecimal,

    @JsonProperty("last_update")
    val lastUpdate: LocalDateTime
)
