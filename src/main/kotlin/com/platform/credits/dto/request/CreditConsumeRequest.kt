package com.platform.credits.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreditConsumeRequest(
    @field:NotBlank(message = "Partner ID is required")
    @Schema(description = "External ID do parceiro", example = "partner-123")
    @JsonProperty("partner_id")
    val partnerId: String,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Valor a ser consumido do saldo", example = "50.00")
    val amount: BigDecimal,

    @field:NotBlank(message = "Description is required")
    @Schema(description = "Descrição da transação", example = "Service usage fee")
    val description: String,

    @Schema(description = "Chave para evitar duplicidade", example = "REQ-123-456-789")
    @JsonProperty("chave_key")
    val chaveKey: String? = null
)
