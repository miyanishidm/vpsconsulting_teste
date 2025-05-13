package com.platform.credits.exception

import com.platform.credits.model.enums.TransactionStatus
import java.util.UUID

class InvalidTransactionStateException(
    val transactionId: UUID,
    val currentStatus: TransactionStatus,
    errorMessage: String = "Transaction $transactionId is in invalid state: $currentStatus"
) : RuntimeException(errorMessage)
