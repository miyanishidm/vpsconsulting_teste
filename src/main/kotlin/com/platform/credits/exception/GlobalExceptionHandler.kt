package com.platform.credits.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.concurrent.ConcurrentHashMap

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Entity not found: ${ex.message}")

        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "Entity not found",
            path = request.requestURI,
            errorCode = "ENTITY_NOT_FOUND"
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InsufficientCreditException::class)
    fun handleInsufficientCredit(ex: InsufficientCreditException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Insufficient credit: ${ex.message}")

        val details = ConcurrentHashMap<String, Any>()
        details["partner_id"] = ex.partnerId
        details["current_balance"] = ex.currentBalance
        details["requested_amount"] = ex.requestedAmount

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Insufficient credit balance",
            path = request.requestURI,
            errorCode = "INSUFFICIENT_CREDIT",
            details = details
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicatedTransactionException::class)
    fun handleDuplicatedTransaction(ex: DuplicatedTransactionException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.info("Duplicated transaction: ${ex.message}")

        val details = ConcurrentHashMap<String, Any>()
        ex.chaveKey?.let { details["chave_key"] = it }
        details["existing_transaction_id"] = ex.existingTransactionId

        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = "Duplicated transaction detected",
            path = request.requestURI,
            errorCode = "DUPLICATED_TRANSACTION",
            details = details
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidTransactionStateException::class)
    fun handleInvalidTransactionState(ex: InvalidTransactionStateException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid transaction state: ${ex.message}")

        val details = ConcurrentHashMap<String, Any>()
        details["transaction_id"] = ex.transactionId
        details["current_status"] = ex.currentStatus

        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message?: "Erro",
            path = request.requestURI,
            errorCode = "INVALID_TRANSACTION_STATE",
            details = details
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Validation error: ${ex.message}")

        val errors = ex.bindingResult.allErrors.map {
            val fieldName = (it as? FieldError)?.field ?: it.objectName
            val message = it.defaultMessage ?: "Validation failed"
            FieldError(fieldName, message)
        }

        val errorResponse = ValidationErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation error",
            path = request.requestURI,
            errors = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception", ex)

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred",
            path = request.requestURI,
            errorCode = "INTERNAL_ERROR"
        )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
