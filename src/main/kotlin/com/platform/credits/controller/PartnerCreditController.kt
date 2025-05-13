package com.platform.credits.controller

import com.platform.credits.dto.request.CreditAddRequest
import com.platform.credits.dto.request.CreditConsumeRequest
import com.platform.credits.dto.request.TransactionFilterRequest
import com.platform.credits.dto.response.CreditBalanceResponse
import com.platform.credits.dto.response.PagedTransactionResponse
import com.platform.credits.dto.response.TransactionResponse
import com.platform.credits.service.PartnerCreditService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/credits")
@Tag(name = "Credits API", description = "API para gerenciamento de créditos de parceiros")
class PartnerCreditController(
    private val partnerCreditService: PartnerCreditService
) {

    @GetMapping("/balance/{partnerId}")
    @Operation(summary = "Consulta o saldo de créditos de um parceiro")
    fun getCreditBalance(@PathVariable partnerId: String): ResponseEntity<CreditBalanceResponse> {
        val balance = partnerCreditService.getCreditBalance(partnerId)
        return ResponseEntity.ok(balance)
    }

    @PostMapping("/add")
    @Operation(summary = "Adiciona créditos a um parceiro")
    fun addCredits(@Valid @RequestBody request: CreditAddRequest): ResponseEntity<TransactionResponse> {
        val transaction = partnerCreditService.addCredits(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @PostMapping("/consume")
    @Operation(summary = "Consome créditos de um parceiro")
    fun consumeCredits(@Valid @RequestBody request: CreditConsumeRequest): ResponseEntity<TransactionResponse> {
        val transaction = partnerCreditService.consumeCredits(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }

    @GetMapping("/transactions/{partnerId}")
    @Operation(summary = "Retorna o histórico de transações de um parceiro")
    fun getTransactionHistory(
        @PathVariable partnerId: String,
        @Valid filter: TransactionFilterRequest
    ): ResponseEntity<PagedTransactionResponse> {
        val pageable = PageRequest.of(
            filter.page,
            filter.size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val transactions = partnerCreditService.getTransactionHistory(
            partnerExternalId = partnerId,
            startDate = filter.startDate,
            endDate = filter.endDate,
            pageable = pageable
        )

        return ResponseEntity.ok(PagedTransactionResponse.fromPage(transactions))
    }
}
