package com.keyfyndr.backend.features.key.domain.usecase

import com.keyfyndr.backend.common.exception.InvalidStatusTransitionException
import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.common.exception.UnauthorizedAccessException
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Marks a key as FOUND.
 *
 * Business rules:
 * - Only the key owner can mark it as found
 * - Only keys with status LOST can transition to FOUND
 * - Inactive (soft-deleted) keys cannot be modified
 *
 * When latitude/longitude are provided, they are stored on the key
 * to power nearby-key queries on the Home Dashboard.
 */
@Component
class MarkKeyAsFoundUseCase(
    private val keyRepository: KeyRepository
) {

    fun execute(keyId: UUID, ownerId: UUID, latitude: Double? = null, longitude: Double? = null): Key {
        val key = keyRepository.findById(keyId)
            ?: throw ResourceNotFoundException("Key not found with ID: $keyId")

        if (!key.isActive) {
            throw ResourceNotFoundException("Key not found with ID: $keyId")
        }

        if (key.ownerId != ownerId) {
            throw UnauthorizedAccessException("You are not authorized to modify this key")
        }

        if (key.status != KeyStatus.LOST) {
            throw InvalidStatusTransitionException(
                "Cannot mark key as FOUND from current status: ${key.status}. " +
                    "Only LOST keys can be marked as FOUND."
            )
        }

        val updatedKey = key.copy(
            status = KeyStatus.FOUND,
            latitude = latitude,
            longitude = longitude,
            lastStatusUpdateAt = Instant.now()
        )
        return keyRepository.save(updatedKey)
    }
}
