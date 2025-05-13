package com.platform.credits.service

import com.platform.credits.dto.request.CreditAddRequest
import com.platform.credits.dto.request.CreditConsumeRequest
import com.platform.credits.dto.response.CreditBalanceResponse
import com.platform.credits.dto.response.TransactionResponse
import com.platform.credits.exception.EntityNotFoundException
import com.platform.credits.exception.InsufficientCreditException
import com.platform.credits.model.Partner
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import com.platform.credits.util.chaveHelper
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class PartnerCreditService(
    private val partnerRepository: PartnerRepository,
    private val transactionService: TransactionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Obtém o saldo de créditos de um parceiro
     */
    fun getCreditBalance(partnerExternalId: String): CreditBalanceResponse {
        val partner = findPartnerByExternalId(partnerExternalId)

        return CreditBalanceResponse(
            partnerId = partner.externalId,
            partnerName = partner.name,
            creditBalance = partner.creditBalance,
            lastUpdate = partner.updatedAt
        )
    }

    /**
     * Adiciona créditos a um parceiro
     */
    @Transactional
    fun addCredits(request: CreditAddRequest): TransactionResponse {
        val partner = findPartnerByExternalId(request.partnerId)

        // Gerar chave se não fornecida
        val chaveKey = request.chaveKey ?:
            chaveHelper.generateKey(request.partnerId, "ADD", request.amount.toString())

        // Criar transação
        val transaction = transactionService.createCreditTransaction(
            partnerId = partner.id,
            amount = request.amount,
            description = request.description,
            chaveKey = chaveKey
        )

        // Se a transação já estiver completa, retornar
        if (transaction.status != com.platform.credits.model.enums.TransactionStatus.PENDING) {
            return mapToTransactionResponse(transaction, partner.externalId)
        }

        // Atualizar saldo do parceiro com lock
        val partnerWithLock = partnerRepository.findByIdWithLock(partner.id)
            .orElseThrow { EntityNotFoundException("Partner", "id", partner.id) }

        partnerWithLock.creditBalance = partnerWithLock.creditBalance.add(request.amount)
        partnerWithLock.updatedAt = LocalDateTime.now()
        partnerRepository.save(partnerWithLock)

        // Completar transação
        val completedTransaction = transactionService.completeTransaction(transaction)

        logger.info("Added credits: {} to partner: {}", request.amount, partner.externalId)

        return mapToTransactionResponse(completedTransaction, partner.externalId)
    }

    /**
     * Consome créditos de um parceiro
     */
    @Transactional
    fun consumeCredits(request: CreditConsumeRequest): TransactionResponse {
        val partner = findPartnerByExternalId(request.partnerId)

        // Verificar se há saldo suficiente
        if (partner.creditBalance.compareTo(request.amount) < 0) {
            throw InsufficientCreditException(
                partnerId = partner.externalId,
                currentBalance = partner.creditBalance,
                requestedAmount = request.amount
            )
        }

        // Gerar chave  se não fornecida
        val chaveKey = request.chaveKey ?:
            chaveHelper.generateKey(request.partnerId, "CONSUME", request.amount.toString())

        // Criar transação
        val transaction = transactionService.createDebitTransaction(
            partnerId = partner.id,
            amount = (partner.creditBalance - request.amount),
            description = request.description,
            chaveKey = chaveKey
        )

        // Se a transação já estiver completa, retornar
        if (transaction.status != com.platform.credits.model.enums.TransactionStatus.PENDING) {
            return mapToTransactionResponse(transaction, partner.externalId)
        }

        try {
            // Atualizar saldo do parceiro com lock
            val partnerWithLock = partnerRepository.findByIdWithLock(partner.id)
                .orElseThrow { EntityNotFoundException("Partner", "id", partner.id) }

            // Verificar novamente o saldo (pode ter mudado após o lock)
            if (partnerWithLock.creditBalance.compareTo(request.amount) < 0) {
                val failReason = "Insufficient balance after lock: ${partnerWithLock.creditBalance}"
                transactionService.failTransaction(transaction, failReason)
                throw InsufficientCreditException(
                    partnerId = partnerWithLock.externalId,
                    currentBalance = partnerWithLock.creditBalance,
                    requestedAmount = request.amount
                )
            }

            partnerWithLock.creditBalance = partnerWithLock.creditBalance.subtract(request.amount)
            partnerWithLock.updatedAt = LocalDateTime.now()
            partnerRepository.save(partnerWithLock)

            // Completar transação
            val completedTransaction = transactionService.completeTransaction(transaction)

            logger.info("Consumed credits: {} from partner: {}", request.amount, partner.externalId)

            return mapToTransactionResponse(completedTransaction, partner.externalId)
        } catch (e: Exception) {
            if (e !is InsufficientCreditException) {
                transactionService.failTransaction(transaction, "Error processing credit consumption: ${e.message}")
            }
            throw e
        }
    }

    /**
     * Obtém o histórico de transações de um parceiro
     */
    fun getTransactionHistory(
        partnerExternalId: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        pageable: Pageable
    ): Page<TransactionResponse> {
        val partner = findPartnerByExternalId(partnerExternalId)

        val transactions = transactionService.getTransactionHistory(partner.id, startDate, endDate, pageable)

        return transactions.map { mapToTransactionResponse(it, partner.externalId) }
    }

    /**
     * Busca um parceiro pelo seu ID externo
     */
    private fun findPartnerByExternalId(externalId: String): Partner {
        return partnerRepository.findByExternalId(externalId)
            .orElseGet {
                // Criar um novo parceiro se não encontrado
                val newPartner = Partner(
                    externalId = externalId,
                    name = "test-partner",
                )
                partnerRepository.save(newPartner)
            }
    }

    /**
     * Converte uma transação para o formato de resposta
     */
    private fun mapToTransactionResponse(transaction: Transaction, partnerExternalId: String): TransactionResponse {
        return TransactionResponse(
            transactionId = transaction.transactionId,
            partnerId = partnerExternalId,
            type = transaction.type,
            amount = transaction.amount,
            description = transaction.description,
            status = transaction.status,
            createdAt = transaction.createdAt,
            completedAt = transaction.completedAt
        )
    }
}
