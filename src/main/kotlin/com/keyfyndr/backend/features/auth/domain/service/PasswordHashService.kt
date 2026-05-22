package com.keyfyndr.backend.features.auth.domain.service

/**
 * Domain service interface for password hashing operations.
 *
 * Use cases depend on this abstraction, not on the BCrypt-backed implementation.
 * The data layer provides the concrete implementation via [PasswordHashServiceImpl].
 */
interface PasswordHashService {

    /**
     * Hashes a raw password string for secure storage.
     */
    fun hash(rawPassword: String): String

    /**
     * Checks whether a raw password matches a previously hashed password.
     */
    fun matches(rawPassword: String, hashedPassword: String): Boolean
}
