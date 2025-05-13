package com.platform.credits.repository

import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    fun findByTransactionId(transactionId: UUID): Optional<Transaction>

    fun findBychaveKey(chaveKey: String): Optional<Transaction>

    fun findByPartnerId(partnerId: Long, pageable: Pageable): Page<Transaction>

    @Query("SELECT t FROM Transaction t WHERE t.partnerId = :partnerId AND t.createdAt BETWEEN :startDate AND :endDate")
    fun findByPartnerIdAndDateRange(
        @Param("partnerId") partnerId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>

    fun countByPartnerIdAndType(partnerId: Long, type: TransactionType): Long

    // Busca transações pendentes que precisam ser reconciliadas
    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.createdAt < :cutoffTime")
    fun findPendingTransactionsForReconciliation(
        @Param("status") status: TransactionStatus = TransactionStatus.PENDING,
        @Param("cutoffTime") cutoffTime: LocalDateTime,
        pageable: Pageable
    ): Page<Transaction>
}
