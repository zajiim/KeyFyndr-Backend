package com.keyfyndr.backend.common.exception

import com.keyfyndr.backend.common.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException (e.g. "email already exists", "invalid OTP", etc.)
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(statusCode = HttpStatus.BAD_REQUEST.value(), message = ex.message ?: "Invalid request")
        )
    }

    /**
     * Handles IllegalStateException (e.g. "Account not verified")
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.error(statusCode = HttpStatus.CONFLICT.value(), message = ex.message ?: "Operation not allowed in current state")
        )
    }

    /**
     * Handles bean validation errors (@NotBlank, @Email, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(statusCode = HttpStatus.BAD_REQUEST.value(), message = errors)
        )
    }

    /**
     * Catch-all for any unhandled exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(), message = "An unexpected error occurred")
        )
    }
}
