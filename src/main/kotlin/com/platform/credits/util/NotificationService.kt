package com.platform.credits.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class NotificationService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,

    @Value("\${app.kafka.notification-topic}")
    private val notificationTopic: String,

    @Value("\${app.notification.credit-threshold}")
    private val creditThreshold: BigDecimal
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Notifica sobre transações significativas
     */
    fun notifySignificantTransaction(transaction: Transaction) {
        if (isSignificantTransaction(transaction)) {
            try {
                val message = mapOf(
                    "transaction_id" to transaction.transactionId.toString(),
                    "partner_id" to transaction.partnerId,
                    "type" to transaction.type.toString(),
                    "amount" to transaction.amount,
                    "status" to transaction.status.toString(),
                    "timestamp" to transaction.createdAt.toString()
                )

                val payload = objectMapper.writeValueAsString(message)
                kafkaTemplate.send(notificationTopic, transaction.transactionId.toString(), payload)

                logger.info("Notification sent for significant transaction: {}", transaction.transactionId)
            } catch (e: Exception) {
                logger.error("Failed to send notification for transaction: {}", transaction.transactionId, e)
                // Não falhar a transação principal em caso de erro na notificação
            }
        }
    }

    /**
     * Verifica se uma transação é significativa o suficiente para notificação
     */
    private fun isSignificantTransaction(transaction: Transaction): Boolean {
        return transaction.amount >= creditThreshold ||
               transaction.type == TransactionType.DEBIT && transaction.amount >= creditThreshold.multiply(BigDecimal("0.5"))
    }
}
