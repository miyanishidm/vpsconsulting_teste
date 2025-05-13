package com.platform.credits.model

import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "transactions", indexes = [
    Index(name = "idx_transaction_partner_id", columnList = "partnerId"),
    Index(name = "idx_transaction_chave_key", columnList = "chaveKey")
])
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val transactionId: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val partnerId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.PENDING,

    @Column(length = 100)
    val chaveKey: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column
    var completedAt: LocalDateTime? = null,

    @Column
    var failureReason: String? = null
)
