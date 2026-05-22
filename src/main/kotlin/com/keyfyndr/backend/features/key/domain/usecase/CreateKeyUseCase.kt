package com.keyfyndr.backend.features.key.domain.usecase

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import com.keyfyndr.backend.features.key.domain.service.KeyIdGeneratorService
import com.keyfyndr.backend.features.key.presentation.request.CreateKeyRequest
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Creates a new key for the authenticated user.
 *
 * Responsibilities:
 * - Generate a unique public key ID via [KeyIdGeneratorService]
 * - Build the domain model with SAFE status
 * - Persist via [KeyRepository]
 */
@Component
class CreateKeyUseCase(
    private val keyRepository: KeyRepository,
    private val keyIdGeneratorService: KeyIdGeneratorService
) {

    fun execute(request: CreateKeyRequest, ownerId: UUID): Key {
        val publicKeyId = keyIdGeneratorService.generateUniqueKeyId()

        val key = Key(
            publicKeyId = publicKeyId,
            title = request.title,
            description = request.description,
            color = request.color,
            category = request.category,
            imageUrl = request.imageUrl,
            status = KeyStatus.SAFE,
            isActive = true,
            ownerId = ownerId
        )

        return keyRepository.save(key)
    }
}
