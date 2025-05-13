package com.platform.credits.dto.request

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class TransactionFilterRequest(
    @Schema(description = "Tipo da transação (CRÉDITO ou DÉBITO)")
    val type: TransactionType? = null,

    @Schema(description = "Status da transação (PENDENTE, CONCLUÍDA ou FALHA)")
    val status: TransactionStatus? = null,

    @Schema(description = "Data de início para filtro", example = "2023-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("start_date")
    val startDate: LocalDateTime? = null,

    @Schema(description = "Data de fim para filtro", example = "2023-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("end_date")
    val endDate: LocalDateTime? = null,

    @Schema(description = "Número da página (começando de 0)", example = "0")
    val page: Int = 0,

    @Schema(description = "Tamanho da página", example = "20")
    val size: Int = 20
)
