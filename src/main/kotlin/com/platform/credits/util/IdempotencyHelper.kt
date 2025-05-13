package com.platform.credits.util

import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.util.Base64

object chaveHelper {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Gera uma chave baseada nos parâmetros fornecidos
     */
    fun generateKey(partnerId: String, action: String, amount: String): String {
        val input = "$partnerId:$action:$amount:${System.currentTimeMillis()}"
        return generateHash(input)
    }

    /**
     * Gera um hash SHA-256 para o input fornecido
     */
    private fun generateHash(input: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            return Base64.getUrlEncoder().encodeToString(hash)
        } catch (e: Exception) {
            logger.error("Error generating hash", e)
            throw RuntimeException("Failed to generate chave key")
        }
    }
}
