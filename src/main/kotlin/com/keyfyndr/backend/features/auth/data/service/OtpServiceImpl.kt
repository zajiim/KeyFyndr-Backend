package com.keyfyndr.backend.features.auth.data.service

import com.keyfyndr.backend.features.auth.domain.service.OtpService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration

/**
 * Redis-backed implementation of [OtpService].
 *
 * Generates 6-digit OTPs, stores them in Redis with a 5-minute TTL,
 * and validates them on verification (deleting on success).
 */
@Service
class OtpServiceImpl(
    private val redisTemplate: StringRedisTemplate
) : OtpService {

    companion object {
        private const val OTP_PREFIX = "otp:"
        private val OTP_TTL: Duration = Duration.ofMinutes(5)
        private const val OTP_LENGTH = 6
    }

    private val secureRandom = SecureRandom()

    /**
     * Generates a 6-digit OTP, stores it in Redis with a 5-minute TTL,
     * and returns the generated OTP string.
     */
    override fun generateAndStore(identifier: String): String {
        val otp = (0 until OTP_LENGTH)
            .map { secureRandom.nextInt(10) }
            .joinToString("")

        redisTemplate.opsForValue()
            .set("$OTP_PREFIX$identifier", otp, OTP_TTL)

        return otp
    }

    /**
     * Retrieves the stored OTP for the given identifier.
     * Returns null if expired or not found.
     */
    override fun getStoredOtp(identifier: String): String? =
        redisTemplate.opsForValue().get("$OTP_PREFIX$identifier")

    /**
     * Validates the provided OTP against the stored OTP.
     * Deletes the OTP from Redis upon successful validation.
     */
    override fun verify(identifier: String, otp: String): Boolean {
        val storedOtp = getStoredOtp(identifier) ?: return false
        if (storedOtp != otp) return false
        redisTemplate.delete("$OTP_PREFIX$identifier")
        return true
    }
}
