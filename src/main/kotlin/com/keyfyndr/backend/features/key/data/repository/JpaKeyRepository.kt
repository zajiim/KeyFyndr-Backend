package com.keyfyndr.backend.features.key.data.repository

import com.keyfyndr.backend.features.key.data.entity.KeyEntity
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA repository for [KeyEntity].
 *
 * This is an infrastructure concern — use cases never interact with this directly.
 * [KeyRepositoryImpl] wraps this and exposes domain models via the domain repository interface.
 */
@Repository
interface JpaKeyRepository : JpaRepository<KeyEntity, UUID> {

    fun findByPublicKeyId(publicKeyId: String): KeyEntity?

    fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID): List<KeyEntity>

    fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID, pageable: Pageable): Page<KeyEntity>

    fun existsByPublicKeyId(publicKeyId: String): Boolean

    /**
     * Soft-delete by setting is_active = false and status = 'INACTIVE'.
     * Uses a direct UPDATE query for efficiency (no need to load the full entity).
     */
    @Modifying
    @Query("UPDATE KeyEntity k SET k.isActive = false, k.status = com.keyfyndr.backend.features.key.domain.enums.KeyStatus.INACTIVE WHERE k.id = :id")
    fun softDeleteById(id: UUID)

    /**
     * Finds all active keys with the given statuses that have non-null location data.
     * Used by the Home Dashboard for nearby-key markers and activity feed.
     */
    @Query(
        """
        SELECT k FROM KeyEntity k
        WHERE k.status IN :statuses
          AND k.isActive = true
          AND k.latitude IS NOT NULL
          AND k.longitude IS NOT NULL
        ORDER BY k.lastStatusUpdateAt DESC
        """
    )
    fun findAllByStatusInAndLocationNotNull(
        @Param("statuses") statuses: List<KeyStatus>
    ): List<KeyEntity>
}
