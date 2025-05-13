package com.platform.credits.service;

import com.platform.credits.exception.EntityNotFoundException
import com.platform.credits.exception.InvalidTransactionStateException
import com.platform.credits.model.Partner
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import com.platform.credits.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.data.domain.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var partnerRepository: PartnerRepository

    @Mock
    private lateinit var notificationService: NotificationService

    @InjectMocks
    private lateinit var transactionService: TransactionService

    private lateinit var partner: Partner
    private lateinit var transaction: Transaction

    @BeforeEach
    fun setUp() {
        partner = Partner(
            id = 1L,
            externalId = "partner-ext-123",
            name = "Test",
            creditBalance = BigDecimal("100.00"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        transaction = Transaction(
            transactionId = UUID.randomUUID(),
            partnerId = partner.id,
            type = TransactionType.CREDIT,
            amount = BigDecimal("50.00"),
            description = "Test credit transaction",
            status = TransactionStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
    }

    @Nested
    @DisplayName("createCreditTransaction Tests")
    inner class CreateCreditTransactionTests {

        @Test
        @DisplayName("deve criar uma transação de crédito com sucesso")
        fun createCreditTransaction_Success_NochaveKey() {
            `when`(partnerRepository.findById(partner.id)).thenReturn(Optional.of(partner))
            `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
                val tx = invocation.getArgument<Transaction>(0)
                tx.copy(transactionId = UUID.randomUUID(), createdAt = LocalDateTime.now())
            }

            val result = transactionService.createCreditTransaction(
                partnerId = partner.id,
                amount = BigDecimal("50.00"),
                description = "New credit"
            )

            assertNotNull(result)
            assertEquals(partner.id, result.partnerId)
            assertEquals(TransactionType.CREDIT, result.type)
            assertEquals(BigDecimal("50.00"), result.amount)
            assertEquals("New credit", result.description)
            assertEquals(TransactionStatus.PENDING, result.status)
            assertNull(result.chaveKey)

            verify(partnerRepository).findById(partner.id)
            verify(transactionRepository).save(any(Transaction::class.java))
        }

        @Test
        @DisplayName("deve retornar a transação existente se a chave  corresponder")
        fun createCreditTransaction_chaveKeyMatch_ReturnsExisting() {
            val chaveKey = "idem-key-123"
            val existingTransaction = transaction.copy(chaveKey = chaveKey, status = TransactionStatus.COMPLETED)
            `when`(transactionRepository.findBychaveKey(chaveKey)).thenReturn(Optional.of(existingTransaction))

            val result = transactionService.createCreditTransaction(
                partnerId = partner.id,
                amount = BigDecimal("50.00"),
                description = "New credit",
                chaveKey = chaveKey
            )

            assertEquals(existingTransaction, result)
            verify(transactionRepository).findBychaveKey(chaveKey)
            verify(partnerRepository, never()).findById(anyLong())
            verify(transactionRepository, never()).save(any(Transaction::class.java))
        }

        @Test
        @DisplayName("deve criar uma nova transação se a chave não for encontrada")
        fun createCreditTransaction_chaveKeyNotFound_CreatesNew() {
            val chaveKey = "idem-key-456"
            `when`(transactionRepository.findBychaveKey(chaveKey)).thenReturn(Optional.empty())
            `when`(partnerRepository.findById(partner.id)).thenReturn(Optional.of(partner))
            `when`(transactionRepository.save(any(Transaction::class.java))).thenAnswer { invocation ->
                 invocation.getArgument<Transaction>(0).copy(transactionId = UUID.randomUUID(), createdAt = LocalDateTime.now(), chaveKey = chaveKey)
            }

            val result = transactionService.createCreditTransaction(
                partnerId = partner.id,
                amount = BigDecimal("70.00"),
                description = "Another credit",
                chaveKey = chaveKey
            )

            assertNotNull(result)
            assertEquals(partner.id, result.partnerId)
            assertEquals(BigDecimal("70.00"), result.amount)
            assertEquals(chaveKey, result.chaveKey)
            assertEquals(TransactionStatus.PENDING, result.status)

            verify(transactionRepository).findBychaveKey(chaveKey)
            verify(partnerRepository).findById(partner.id)
            verify(transactionRepository).save(any(Transaction::class.java))
        }

        @Test
        @DisplayName("deve lançar EntityNotFoundException quando o parceiro não existir")
        fun createCreditTransaction_PartnerNotFound_ThrowsEntityNotFoundException() {
            val nonExistentPartnerId = 999L
            `when`(partnerRepository.findById(nonExistentPartnerId)).thenReturn(Optional.empty())

            val exception = assertThrows<EntityNotFoundException> {
                transactionService.createCreditTransaction(
                    partnerId = nonExistentPartnerId,
                    amount = BigDecimal("50.00"),
                    description = "Credit attempt"
                )
            }

            assertEquals("Partner not found with id: 999", exception.message)
            verify(partnerRepository).findById(nonExistentPartnerId)
            verify(transactionRepository, never()).save(any(Transaction::class.java))
        }
    }
}

