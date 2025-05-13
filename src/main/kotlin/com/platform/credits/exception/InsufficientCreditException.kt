package com.platform.credits.exception

import java.math.BigDecimal

class InsufficientCreditException(
    val partnerId: String,
    val currentBalance: BigDecimal,
    val requestedAmount: BigDecimal
) : RuntimeException("Partner $partnerId has insufficient credit. Current balance: $currentBalance, Requested: $requestedAmount")
