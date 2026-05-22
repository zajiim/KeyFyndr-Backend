package com.keyfyndr.backend.features.key.domain.usecase

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Retrieves active keys owned by the authenticated user with pagination support.
 *
 * Only returns keys where isActive = true (soft-deleted keys are excluded).
 */
@Component
class GetMyKeysUseCase(
    private val keyRepository: KeyRepository
) {

    fun execute(ownerId: UUID, page: Int, size: Int): PageResult<Key> {
        return keyRepository.findAllByOwnerIdAndIsActiveTrue(ownerId, page, size)
    }
}
