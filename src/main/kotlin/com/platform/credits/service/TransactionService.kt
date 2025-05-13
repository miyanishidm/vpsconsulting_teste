package com.platform.credits.service

import com.platform.credits.exception.EntityNotFoundException
import com.platform.credits.exception.InvalidTransactionStateException
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import com.platform.credits.repository.TransactionRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val partnerRepository: PartnerRepository,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Cria uma nova transação do tipo CREDIT (adição de créditos)
     */
    @Transactional
    fun createCreditTransaction(
        partnerId: Long,
        amount: BigDecimal,
        description: String,
        chaveKey: String? = null
    ): Transaction {
        // Verificar chave
        if (chaveKey != null) {
            val existingTransaction = transactionRepository.findBychaveKey(chaveKey)
            if (existingTransaction.isPresent) {
                logger.info("Found existing transaction with chave key: {}", chaveKey)
                return existingTransaction.get()
            }
        }

        // Verificar se o parceiro existe
        val partner = partnerRepository.findById(partnerId)
            .orElseThrow { EntityNotFoundException("Partner", "id", partnerId) }

        // Criar transação
        val transaction = Transaction(
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = amount,
            description = description,
            chaveKey = chaveKey,
            status = TransactionStatus.PENDING
        )

        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Created credit transaction: {}", savedTransaction.transactionId)

        return savedTransaction
    }

    /**
     * Cria uma nova transação do tipo DEBIT (consumo de créditos)
     */
    @Transactional
    fun createDebitTransaction(
        partnerId: Long,
        amount: BigDecimal,
        description: String,
        chaveKey: String? = null
    ): Transaction {
        // Verificar chave
        if (chaveKey != null) {
            val existingTransaction = transactionRepository.findBychaveKey(chaveKey)
            if (existingTransaction.isPresent) {
                logger.info("Found existing transaction with chave key: {}", chaveKey)
                return existingTransaction.get()
            }
        }

        // Verificar se o parceiro existe
        val partner = partnerRepository.findById(partnerId)
            .orElseThrow { EntityNotFoundException("Partner", "id", partnerId) }

        // Criar transação
        val transaction = Transaction(
            partnerId = partnerId,
            type = TransactionType.DEBIT,
            amount = amount,
            description = description,
            chaveKey = chaveKey,
            status = TransactionStatus.PENDING
        )

        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Created debit transaction: {}", savedTransaction.transactionId)

        return savedTransaction
    }

    /**
     * Completa uma transação pendente
     */
    @Transactional
    fun completeTransaction(transaction: Transaction): Transaction {
        if (transaction.status != TransactionStatus.PENDING) {
            throw InvalidTransactionStateException(
                transaction.transactionId,
                transaction.status,
                "Cannot complete transaction that is not in PENDING state"
            )
        }

        transaction.status = TransactionStatus.COMPLETED
        transaction.completedAt = LocalDateTime.now()
        transaction.updatedAt = LocalDateTime.now()

        val completedTransaction = transactionRepository.save(transaction)
        logger.info("Completed transaction: {}", completedTransaction.transactionId)

        // Notificar sobre transações significativas
        notificationService.notifySignificantTransaction(completedTransaction)

        return completedTransaction
    }

    /**
     * Marca uma transação como falha
     */
    @Transactional
    fun failTransaction(transaction: Transaction, reason: String): Transaction {
        if (transaction.status != TransactionStatus.PENDING) {
            throw InvalidTransactionStateException(
                transaction.transactionId,
                transaction.status,
                "Cannot fail transaction that is not in PENDING state"
            )
        }

        transaction.status = TransactionStatus.FAILED
        transaction.failureReason = reason
        transaction.updatedAt = LocalDateTime.now()

        val failedTransaction = transactionRepository.save(transaction)
        logger.info("Failed transaction: {} with reason: {}", failedTransaction.transactionId, reason)

        return failedTransaction
    }

    /**
     * Busca uma transação pelo seu ID
     */
    fun findTransactionById(transactionId: UUID): Transaction {
        return transactionRepository.findByTransactionId(transactionId)
            .orElseThrow { EntityNotFoundException("Transaction", "id", transactionId) }
    }

    /**
     * Busca o histórico de transações de um parceiro
     */
    fun getTransactionHistory(
        partnerId: Long,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        pageable: Pageable
    ): Page<Transaction> {
        // Verificar se o parceiro existe
        partnerRepository.findById(partnerId)
            .orElseThrow { EntityNotFoundException("Partner", "id", partnerId) }

        // Buscar transações com filtros
        return if (startDate != null && endDate != null) {
            transactionRepository.findByPartnerIdAndDateRange(partnerId, startDate, endDate, pageable)
        } else {
            transactionRepository.findByPartnerId(partnerId, pageable)
        }
    }

    /**
     * Busca transações pendentes para reconciliação
     */
    fun findPendingTransactionsForReconciliation(cutoffTime: LocalDateTime, pageSize: Int = 50): Page<Transaction> {
        val pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"))
        return transactionRepository.findPendingTransactionsForReconciliation(TransactionStatus.PENDING, cutoffTime, pageable)
    }
}
