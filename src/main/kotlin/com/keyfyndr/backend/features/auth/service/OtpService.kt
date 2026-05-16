package com.keyfyndr.backend.features.auth.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration

@Service
class OtpService(
    private val redisTemplate: StringRedisTemplate
) {

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
    fun generateAndStore(email: String): String {
        val otp = (0 until OTP_LENGTH)
            .map { secureRandom.nextInt(10) }
            .joinToString("")

        redisTemplate.opsForValue()
            .set("$OTP_PREFIX$email", otp, OTP_TTL)

        return otp
    }

    /**
     * Retrieves the stored OTP for the given email.
     * Returns null if expired or not found.
     */
    fun getStoredOtp(email: String): String? =
        redisTemplate.opsForValue().get("$OTP_PREFIX$email")

    /**
     * Validates the provided OTP against the stored OTP.
     * Deletes the OTP from Redis upon successful validation.
     */
    fun verify(email: String, otp: String): Boolean {
        val storedOtp = getStoredOtp(email) ?: return false
        if (storedOtp != otp) return false
        redisTemplate.delete("$OTP_PREFIX$email")
        return true
    }
}
