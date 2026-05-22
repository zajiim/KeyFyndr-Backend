package com.keyfyndr.backend.features.key.data.service

import com.keyfyndr.backend.features.key.data.repository.JpaKeyRepository
import com.keyfyndr.backend.features.key.domain.service.KeyIdGeneratorService
import org.springframework.stereotype.Service

/**
 * Generates unique public key IDs in the format: KF-XXXXXX
 *
 * Design decisions:
 * - Uses a restricted charset that excludes ambiguous characters (O/0, I/1)
 *   to improve readability when keys are shared verbally or printed
 * - Retries on collision (extremely unlikely with 30^6 ≈ 729M combinations)
 * - Max 10 retries to prevent infinite loops in pathological cases
 */
@Service
class KeyIdGeneratorServiceImpl(
    private val jpaKeyRepository: JpaKeyRepository
) : KeyIdGeneratorService {

    companion object {
        /** Prefix for all public key IDs */
        private const val PREFIX = "KF"

        /** Length of the random portion */
        private const val RANDOM_LENGTH = 6

        /** Maximum retry attempts to find a unique ID */
        private const val MAX_RETRIES = 10

        /**
         * Charset excluding ambiguous characters: O (Oscar), 0 (zero), I (India), 1 (one).
         * This makes IDs easier to read, share verbally, and print on physical labels.
         */
        private const val CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }

    override fun generateUniqueKeyId(): String {
        repeat(MAX_RETRIES) {
            val randomPart = (1..RANDOM_LENGTH)
                .map { CHARSET.random() }
                .joinToString("")

            val publicKeyId = "$PREFIX-$randomPart"

            if (!jpaKeyRepository.existsByPublicKeyId(publicKeyId)) {
                return publicKeyId
            }
        }

        throw IllegalStateException(
            "Failed to generate a unique public key ID after $MAX_RETRIES attempts. " +
                "This indicates an extremely unlikely collision scenario."
        )
    }
}
