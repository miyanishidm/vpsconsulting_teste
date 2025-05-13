package com.platform.credits.service;

import com.platform.credits.dto.request.CreditAddRequest
import com.platform.credits.dto.request.CreditConsumeRequest
import com.platform.credits.dto.response.CreditBalanceResponse
import com.platform.credits.dto.response.TransactionResponse
import com.platform.credits.exception.EntityNotFoundException
import com.platform.credits.exception.InsufficientCreditException
import com.platform.credits.model.Partner
import com.platform.credits.model.Transaction
import com.platform.credits.model.enums.TransactionStatus
import com.platform.credits.model.enums.TransactionType
import com.platform.credits.repository.PartnerRepository
import com.platform.credits.util.chaveHelper
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PartnerCreditServiceTest {

    @Mock
    private lateinit var partnerRepository: PartnerRepository

    @Mock
    private lateinit var transactionService: TransactionService

    @InjectMocks
    private lateinit var partnerCreditService: PartnerCreditService

    private lateinit var partner: Partner
    private lateinit var transaction: Transaction

    @BeforeEach
    fun setUp() {
        partner = Partner(
            id = 1L,
            externalId = "partner-ext-123",
            name = "Test Partner",
            creditBalance = BigDecimal("100.00"),
            createdAt = LocalDateTime.now().minusDays(1),
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
    @DisplayName("getCreditBalance Tests")
    inner class GetCreditBalanceTests {

        @Test
        @DisplayName("deve devolver o saldo credor para o parceiro existente")
        fun getCreditBalance_PartnerExists_ReturnsBalance() {
            `when`(partnerRepository.findByExternalId(partner.externalId)).thenReturn(Optional.of(partner))

            val result = partnerCreditService.getCreditBalance(partner.externalId)

            assertNotNull(result)
            assertEquals(partner.externalId, result.partnerId)
            assertEquals(partner.name, result.partnerName)
            assertEquals(partner.creditBalance, result.creditBalance)
            assertEquals(partner.updatedAt, result.lastUpdate)

            verify(partnerRepository).findByExternalId(partner.externalId)
        }

        @Test
        @DisplayName("deve criar um novo parceiro e retornar saldo zero se o parceiro não for encontrado")
        fun getCreditBalance_PartnerNotFound_CreatesNewAndReturnsZeroBalance() {
            val newPartnerExternalId = "new-partner-ext-456"
            val captor = argumentCaptor<Partner>()
            `when`(partnerRepository.findByExternalId(newPartnerExternalId)).thenReturn(Optional.empty())
            `when`(partnerRepository.save(captor.capture())).thenAnswer { invocation -> invocation.getArgument(0) }

            val result = partnerCreditService.getCreditBalance(newPartnerExternalId)

            assertNotNull(result)
            assertEquals(newPartnerExternalId, result.partnerId)
            assertEquals("test-partner", result.partnerName)
            assertEquals(BigDecimal.ZERO, result.creditBalance)
            assertNotNull(result.lastUpdate)

            verify(partnerRepository).findByExternalId(newPartnerExternalId)
            verify(partnerRepository).save(any(Partner::class.java))
            val savedPartner = captor.firstValue
            assertEquals(newPartnerExternalId, savedPartner.externalId)
            assertEquals("test-partner", savedPartner.name)
            assertEquals(BigDecimal.ZERO, savedPartner.creditBalance)
        }
    }

}
