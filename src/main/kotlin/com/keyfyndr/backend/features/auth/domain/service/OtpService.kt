package com.keyfyndr.backend.features.auth.domain.service

/**
 * Domain service interface for OTP (One-Time Password) operations.
 *
 * Use cases depend on this abstraction, not on the Redis-backed implementation.
 * The data layer provides the concrete implementation via [OtpServiceImpl].
 */
interface OtpService {

    /**
     * Generates a new OTP for the given identifier (email or phone),
     * stores it with a TTL, and returns the generated OTP string.
     */
    fun generateAndStore(identifier: String): String

    /**
     * Retrieves the stored OTP for the given identifier.
     * Returns null if expired or not found.
     */
    fun getStoredOtp(identifier: String): String?

    /**
     * Validates the provided OTP against the stored OTP.
     * Deletes the OTP upon successful validation.
     */
    fun verify(identifier: String, otp: String): Boolean
}
