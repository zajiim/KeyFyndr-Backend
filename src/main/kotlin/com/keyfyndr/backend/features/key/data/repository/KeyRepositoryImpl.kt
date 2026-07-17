package com.keyfyndr.backend.features.key.data.repository

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.key.data.mapper.toDomain
import com.keyfyndr.backend.features.key.data.mapper.toEntity
import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.domain.repository.KeyRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Data layer implementation of [KeyRepository].
 *
 * Bridges the domain layer to the JPA persistence layer using mappers.
 * All JPA/entity concerns are encapsulated here — use cases only see domain models.
 */
@Repository
class KeyRepositoryImpl(
    private val jpaKeyRepository: JpaKeyRepository
) : KeyRepository {

    override fun save(key: Key): Key {
        val entity = key.toEntity()
        val savedEntity = jpaKeyRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): Key? =
        jpaKeyRepository.findById(id).orElse(null)?.toDomain()

    override fun findByPublicKeyId(publicKeyId: String): Key? =
        jpaKeyRepository.findByPublicKeyId(publicKeyId)?.toDomain()

    override fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID): List<Key> =
        jpaKeyRepository.findAllByOwnerIdAndIsActiveTrue(ownerId).map { it.toDomain() }

    override fun findAllByOwnerIdAndIsActiveTrue(ownerId: UUID, page: Int, size: Int): PageResult<Key> {
        // API uses 1-indexed pages, Spring Data uses 0-indexed
        val pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val springPage = jpaKeyRepository.findAllByOwnerIdAndIsActiveTrue(ownerId, pageable)

        return PageResult(
            content = springPage.content.map { it.toDomain() },
            currentPage = page,
            pageSize = size,
            totalItems = springPage.totalElements,
            totalPages = springPage.totalPages
        )
    }

    override fun existsByPublicKeyId(publicKeyId: String): Boolean =
        jpaKeyRepository.existsByPublicKeyId(publicKeyId)

    @Transactional
    override fun softDelete(id: UUID) {
        jpaKeyRepository.softDeleteById(id)
    }

    override fun findLatestActiveByOwnerId(ownerId: UUID, limit: Int): List<Key> {
        val pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        return jpaKeyRepository.findAllByOwnerIdAndIsActiveTrue(ownerId, pageable)
            .content.map { it.toDomain() }
    }

    override fun findAllByStatusInAndLocationNotNull(statuses: List<KeyStatus>): List<Key> =
        jpaKeyRepository.findAllByStatusInAndLocationNotNull(statuses).map { it.toDomain() }
}
