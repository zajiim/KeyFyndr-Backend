package com.keyfyndr.backend.common.exception

/**
 * Thrown when a requested resource is not found.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * Thrown when a user attempts to access or modify a resource they don't own.
 * Mapped to HTTP 403 by GlobalExceptionHandler.
 */
class UnauthorizedAccessException(message: String) : RuntimeException(message)

/**
 * Thrown when an invalid state transition is attempted (e.g., marking a SAFE key as FOUND).
 * Mapped to HTTP 400 by GlobalExceptionHandler.
 */
class InvalidStatusTransitionException(message: String) : RuntimeException(message)
