package com.platform.credits.exception

import java.util.UUID

class DuplicatedTransactionException(
    val chaveKey: String?,
    val existingTransactionId: UUID
) : RuntimeException("Transaction with chave key $chaveKey already exists (transaction ID: $existingTransactionId)")
