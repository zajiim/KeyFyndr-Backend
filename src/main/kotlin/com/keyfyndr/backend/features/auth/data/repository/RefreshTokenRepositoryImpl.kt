package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.mapper.toDomain
import com.keyfyndr.backend.features.auth.data.mapper.toEntity
import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Data layer implementation of [RefreshTokenRepository].
 * Bridges the domain layer to the JPA persistence layer using mappers.
 */
@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRefreshTokenRepository: JpaRefreshTokenRepository
) : RefreshTokenRepository {

    override fun findByToken(token: String): RefreshToken? =
        jpaRefreshTokenRepository.findByToken(token)?.toDomain()

    override fun save(refreshToken: RefreshToken): RefreshToken {
        val entity = refreshToken.toEntity()
        val savedEntity = jpaRefreshTokenRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun delete(refreshToken: RefreshToken) {
        jpaRefreshTokenRepository.delete(refreshToken.toEntity())
    }

    @Transactional
    override fun deleteByUserId(userId: UUID) {
        jpaRefreshTokenRepository.deleteByUserId(userId)
    }
}
