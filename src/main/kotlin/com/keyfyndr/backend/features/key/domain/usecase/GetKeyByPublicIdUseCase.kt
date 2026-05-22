package com.keyfyndr.backend.features.key.domain.usecase

import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component

/**
 * Retrieves a key by its human-readable public ID (e.g., "KF-8G2X9P").
 *
 * This is a public-facing endpoint — no authentication required.
 * Only returns active keys to prevent leaking deleted key data.
 */
@Component
class GetKeyByPublicIdUseCase(
    private val keyRepository: KeyRepository
) {

    fun execute(publicKeyId: String): Key {
        val key = keyRepository.findByPublicKeyId(publicKeyId)
            ?: throw ResourceNotFoundException("Key not found with public ID: $publicKeyId")

        if (!key.isActive) {
            throw ResourceNotFoundException("Key not found with public ID: $publicKeyId")
        }

        return key
    }
}
