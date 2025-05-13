package com.platform.credits.model.enums

enum class TransactionStatus {
    PENDING,   // Transação iniciada mas não concluída
    COMPLETED, // Transação processada com sucesso
    FAILED     // Falha no processamento da transação
}
