package com.platform.credits.service

import com.platform.credits.model.Partner
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReconciliationService(
    private val transactionService: TransactionService,
    private val partnerRepository: PartnerRepository,

    @Value("\${app.reconciliation.transaction-timeout-minutes}")
    private val transactionTimeoutMinutes: Long = 15,

    @Value("\${app.reconciliation.batch-size}")
    private val batchSize: Int = 50
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Executa a reconciliação de transações pendentes
     * Agendado para executar a cada 5 minutos
     */
    @Scheduled(fixedDelayString = "\${app.reconciliation.fixed-delay-ms:300000}")
    @Transactional
    fun reconcilePendingTransactions() {
        logger.info("Starting reconciliation of pending transactions")

        val cutoffTime = LocalDateTime.now().minusMinutes(transactionTimeoutMinutes)
        var processedCount = 0
        var page = transactionService.findPendingTransactionsForReconciliation(cutoffTime, batchSize)

        while (page.hasContent()) {
            for (transaction in page.content) {
                try {
                    reconcileTransaction(transaction)
                    processedCount++
                } catch (e: Exception) {
                    logger.error("Error reconciling transaction {}: {}", transaction.transactionId, e.message, e)
                }
            }

            if (page.hasNext()) {
                page = transactionService.findPendingTransactionsForReconciliation(cutoffTime, batchSize)
            } else {
                break
            }
        }

        logger.info("Completed reconciliation, processed {} pending transactions", processedCount)
    }

    /**
     * Reconcilia uma transação pendente
     */
    @Transactional
    fun reconcileTransaction(transaction: Transaction) {
        if (transaction.status != TransactionStatus.PENDING) {
            logger.info("Transaction {} is not pending, skipping reconciliation", transaction.transactionId)
            return
        }

        logger.info("Reconciling transaction: {}", transaction.transactionId)

        val partnerOpt = partnerRepository.findByIdWithLock(transaction.partnerId)
        if (partnerOpt.isEmpty) {
            transactionService.failTransaction(transaction, "Parceiro não encontrado durante a reconciliação")
            return
        }

        val partner = partnerOpt.get()

        when (transaction.type) {
            TransactionType.CREDIT -> reconcileCreditTransaction(transaction, partner)
            TransactionType.DEBIT -> reconcileDebitTransaction(transaction, partner)
        }
    }

    /**
     * Reconcilia uma transação de crédito pendente
     */
    private fun reconcileCreditTransaction(transaction: Transaction, partner: Partner) {
        try {
            // Adicionar créditos ao parceiro
            partner.creditBalance = partner.creditBalance.add(transaction.amount)
            partner.updatedAt = LocalDateTime.now()
            partnerRepository.save(partner)

            // Completar transação
            transactionService.completeTransaction(transaction)

            logger.info("Reconciled credit transaction: {}", transaction.transactionId)
        } catch (e: Exception) {
            logger.error("Failed to reconcile credit transaction: {}", transaction.transactionId, e)
            transactionService.failTransaction(transaction, "Reconciliation failed: ${e.message}")
        }
    }

    /**
     * Reconcilia uma transação de débito pendente
     */
    private fun reconcileDebitTransaction(transaction: Transaction, partner: Partner) {
        try {
            // Verificar se há saldo suficiente
            if (partner.creditBalance.compareTo(transaction.amount) < 0) {
                transactionService.failTransaction(
                    transaction,
                    "Insufficient balance during reconciliation: current=${partner.creditBalance}, required=${transaction.amount}"
                )
                return
            }

            // Consumir créditos do parceiro
            partner.creditBalance = partner.creditBalance.subtract(transaction.amount)
            partner.updatedAt = LocalDateTime.now()
            partnerRepository.save(partner)

            // Completar transação
            transactionService.completeTransaction(transaction)

            logger.info("Reconciled debit transaction: {}", transaction.transactionId)
        } catch (e: Exception) {
            logger.error("Failed to reconcile debit transaction: {}", transaction.transactionId, e)
            transactionService.failTransaction(transaction, "Reconciliation failed: ${e.message}")
        }
    }
}
