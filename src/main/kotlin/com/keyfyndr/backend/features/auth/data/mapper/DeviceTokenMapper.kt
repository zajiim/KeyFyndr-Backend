package com.keyfyndr.backend.features.auth.data.mapper

import com.keyfyndr.backend.features.auth.domain.model.DeviceToken
import com.keyfyndr.backend.features.auth.data.entity.DeviceTokenEntity

fun DeviceTokenEntity.toDomain(): DeviceToken = DeviceToken(
    id = this.id,
    userId = this.userId,
    deviceToken = this.deviceToken,
    deviceType = this.deviceType
)

fun DeviceToken.toEntity(): DeviceTokenEntity = DeviceTokenEntity(
    id = this.id,
    userId = this.userId,
    deviceToken = this.deviceToken,
    deviceType = this.deviceType
)
