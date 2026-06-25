package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.entity.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface JpaDeviceTokenRepository : JpaRepository<DeviceTokenEntity, UUID> {

    fun findByUserId(userId: UUID): List<DeviceTokenEntity>

    fun existsByDeviceToken(deviceToken: String): Boolean

    @Modifying
    @Transactional
    fun deleteByDeviceToken(deviceToken: String)

    @Modifying
    @Transactional
    fun deleteByUserId(userId: UUID)

    @Modifying
    @Transactional
    @Query("DELETE FROM DeviceTokenEntity e WHERE e.deviceToken IN :tokens")
    fun deleteByDeviceTokenIn(tokens: List<String>)
}

