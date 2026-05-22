package com.keyfyndr.backend.features.key.domain.usecase

import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.common.exception.UnauthorizedAccessException
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Soft-deletes a key by setting isActive = false.
 *
 * Business rules:
 * - Only the key owner can delete their key
 * - The key record is preserved in the database (soft delete)
 * - Already inactive keys return a 404
 */
@Component
class DeleteKeyUseCase(
    private val keyRepository: KeyRepository
) {

    fun execute(keyId: UUID, ownerId: UUID) {
        val key = keyRepository.findById(keyId)
            ?: throw ResourceNotFoundException("Key not found with ID: $keyId")

        if (!key.isActive) {
            throw ResourceNotFoundException("Key not found with ID: $keyId")
        }

        if (key.ownerId != ownerId) {
            throw UnauthorizedAccessException("You are not authorized to delete this key")
        }

        keyRepository.softDelete(keyId)
    }
}
