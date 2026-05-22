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
     * Handles ResourceNotFoundException (e.g. "Key not found")
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(statusCode = HttpStatus.NOT_FOUND.value(), message = ex.message ?: "Resource not found")
        )
    }

    /**
     * Handles UnauthorizedAccessException (e.g. "You are not authorized to modify this key")
     */
    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedAccess(ex: UnauthorizedAccessException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error(statusCode = HttpStatus.FORBIDDEN.value(), message = ex.message ?: "Access denied")
        )
    }

    /**
     * Handles InvalidStatusTransitionException (e.g. "Cannot mark SAFE key as FOUND")
     */
    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleInvalidStatusTransition(ex: InvalidStatusTransitionException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.error(statusCode = HttpStatus.BAD_REQUEST.value(), message = ex.message ?: "Invalid status transition")
        )
    }

    /**
     * Catch-all for any unhandled exceptions.
     * Logs the full stack trace so 500 errors are diagnosable from server console.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        ex.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(), message = "An unexpected error occurred")
        )
    }
}
