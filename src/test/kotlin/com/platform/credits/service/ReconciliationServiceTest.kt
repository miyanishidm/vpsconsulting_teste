package com.platform.credits.service;

import com.platform.credits.model.Partner
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.util.ReflectionTestUtils
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ReconciliationServiceTest {

    @Mock
    private lateinit var transactionService: TransactionService

    @Mock
    private lateinit var partnerRepository: PartnerRepository

    private lateinit var reconciliationService: ReconciliationService

    @BeforeEach
    fun setUp() {
        reconciliationService = ReconciliationService(
            transactionService = transactionService,
            partnerRepository = partnerRepository,
            transactionTimeoutMinutes = 15L,
            batchSize = 50
        )
    }

    @Test
    fun `should reconcile pending credit transaction successfully`() {
        // Given
        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            partnerId = 1L,
            amount = BigDecimal("100.00"),
            type = TransactionType.CREDIT,
            status = TransactionStatus.PENDING,
            createdAt = LocalDateTime.now().minusMinutes(20),
            completedAt = null,
            description = "Test credit transaction"
        )

        val partner = Partner(
            id = 1L,
            name = "Test Partner",
            creditBalance = BigDecimal("200.00"),
            externalId = "Partner 123",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1)
        )

        val cutoffTime = LocalDateTime.now().minusMinutes(15)
        val page = PageImpl(listOf(transaction))

        `when`(transactionService.findPendingTransactionsForReconciliation(any(), anyInt()))
            .thenReturn(page)
        `when`(partnerRepository.findByIdWithLock(1L))
            .thenReturn(Optional.of(partner))

        // When
        reconciliationService.reconcilePendingTransactions()

        // Then
        verify(partnerRepository).save(partner)
        verify(transactionService).completeTransaction(transaction)

        // Check partner balance was updated correctly
        assert(partner.creditBalance == BigDecimal("300.00")) { "Partner balance should be 300.00 after credit" }
    }

    @Test
    fun `should reconcile pending debit transaction successfully`() {
        // Given
        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            partnerId = 1L,
            amount = BigDecimal("50.00"),
            type = TransactionType.DEBIT,
            status = TransactionStatus.PENDING,
            createdAt = LocalDateTime.now().minusMinutes(20),
            completedAt = null,
            description = "Test debit transaction"
        )

        val partner = Partner(
            id = 1L,
            name = "Test Partner",
            creditBalance = BigDecimal("200.00"),
            externalId = "Partner 123",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now().minusDays(1)
        )

        val cutoffTime = LocalDateTime.now().minusMinutes(15)
        val page = PageImpl(listOf(transaction))

        `when`(transactionService.findPendingTransactionsForReconciliation(any(), anyInt()))
            .thenReturn(page)
        `when`(partnerRepository.findByIdWithLock(1L))
            .thenReturn(Optional.of(partner))

        // When
        reconciliationService.reconcilePendingTransactions()

        // Then
        verify(partnerRepository).save(partner)
        verify(transactionService).completeTransaction(transaction)

        // Check partner balance was updated correctly
        assert(partner.creditBalance == BigDecimal("150.00")) { "Partner balance should be 150.00 after debit" }
    }

    @Test
    fun `should handle partner not found during reconciliation`() {
        // Given
        val transaction = Transaction(
            transactionId = UUID.randomUUID(),
            partnerId = 999L,
            amount = BigDecimal("100.00"),
            type = TransactionType.CREDIT,
            status = TransactionStatus.PENDING,
            createdAt = LocalDateTime.now().minusMinutes(20),
            completedAt = null,
            description = "Test partner not found transaction"
        )

        val page = PageImpl(listOf(transaction))

        `when`(transactionService.findPendingTransactionsForReconciliation(any(), anyInt()))
            .thenReturn(page)
        `when`(partnerRepository.findByIdWithLock(999L))
            .thenReturn(Optional.empty())

        // When
        reconciliationService.reconcilePendingTransactions()

        // Then
        verify(partnerRepository, never()).save(any())
    }
}
