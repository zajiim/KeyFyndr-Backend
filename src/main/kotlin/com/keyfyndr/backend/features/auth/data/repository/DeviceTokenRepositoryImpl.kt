package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.mapper.toDomain
import com.keyfyndr.backend.features.auth.data.mapper.toEntity
import com.keyfyndr.backend.features.auth.domain.model.DeviceToken
import com.keyfyndr.backend.features.auth.domain.repository.DeviceTokenRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Data layer implementation of [DeviceTokenRepository].
 * Bridges the domain layer to the JPA persistence layer using mappers.
 */
@Repository
class DeviceTokenRepositoryImpl(
    private val jpaDeviceTokenRepository: JpaDeviceTokenRepository
) : DeviceTokenRepository {

    override fun save(deviceToken: DeviceToken): DeviceToken {
        val entity = deviceToken.toEntity()
        return jpaDeviceTokenRepository.save(entity).toDomain()
    }

    override fun findByUserId(userId: UUID): List<DeviceToken> =
        jpaDeviceTokenRepository.findByUserId(userId).map { it.toDomain() }

    override fun deleteByToken(token: String) =
        jpaDeviceTokenRepository.deleteByDeviceToken(token)

    override fun deleteByUserId(userId: UUID) =
        jpaDeviceTokenRepository.deleteByUserId(userId)

    override fun existsByDeviceToken(token: String): Boolean =
        jpaDeviceTokenRepository.existsByDeviceToken(token)

    override fun deleteByTokens(tokens: List<String>) {
        if (tokens.isNotEmpty()) {
            jpaDeviceTokenRepository.deleteByDeviceTokenIn(tokens)
        }
    }
}
