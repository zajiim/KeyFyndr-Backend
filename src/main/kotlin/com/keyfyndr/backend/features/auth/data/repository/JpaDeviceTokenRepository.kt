package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.entity.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaDeviceTokenRepository : JpaRepository<DeviceTokenEntity, UUID> {

    fun findByUserId(userId: UUID): List<DeviceTokenEntity>
}
