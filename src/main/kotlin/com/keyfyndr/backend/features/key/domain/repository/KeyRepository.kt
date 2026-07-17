package com.keyfyndr.backend.features.key.domain.repository

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import com.keyfyndr.backend.features.key.domain.model.Key
import java.util.UUID

/**
 * Domain repository interface for Key operations.
 *
 * Use cases depend on this abstraction, not on the JPA implementation.
 * The data layer provides the concrete implementation via [KeyRepositoryImpl].
 */
interface KeyRepository {

    fun save(key: Key): Key

    fun findById(id: UUID): Key?

    fun findByPublicKeyId(publicKeyId: String): Key?

    fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID): List<Key>

    /** Paginated version — page is 1-indexed for API consistency. */
    fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID, page: Int, size: Int): PageResult<Key>

    fun existsByPublicKeyId(publicKeyId: String): Boolean

    /**
     * Soft-deletes a key by setting isActive = false and status = INACTIVE.
     * The record remains in the database for audit/recovery purposes.
     */
    fun softDelete(id: UUID)

    /**
     * Returns the latest [limit] active keys for the owner, sorted by createdAt DESC.
     * Used by the Home Dashboard for the "My Keys Preview" section.
     */
    fun findLatestActiveByOwnerId(ownerId: UUID, limit: Int): List<Key>

    /**
     * Returns all active keys with the given statuses that have location data.
     * Used by the Home Dashboard for nearby-key queries (markers + activity feed).
     */
    fun findAllByStatusInAndLocationNotNull(statuses: List<KeyStatus>): List<Key>
}
