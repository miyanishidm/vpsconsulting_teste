package com.platform.credits.repository

import com.platform.credits.model.Partner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import jakarta.persistence.LockModeType

@Repository
interface PartnerRepository : JpaRepository<Partner, Long> {

    fun findByExternalId(externalId: String): Optional<Partner>

    // Método com lock pessimista para evitar condições de corrida ao atualizar saldo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partner p WHERE p.id = :id")
    fun findByIdWithLock(@Param("id") id: Long): Optional<Partner>
}
