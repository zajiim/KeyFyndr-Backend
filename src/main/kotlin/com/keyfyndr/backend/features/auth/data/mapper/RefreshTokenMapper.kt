package com.keyfyndr.backend.features.auth.data.mapper

import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import com.keyfyndr.backend.features.auth.data.entity.RefreshTokenEntity

fun RefreshTokenEntity.toDomain(): RefreshToken = RefreshToken(
    id = this.id,
    userId = this.userId,
    token = this.token,
    expiryDate = this.expiryDate
)

fun RefreshToken.toEntity(): RefreshTokenEntity = RefreshTokenEntity(
    id = this.id,
    userId = this.userId,
    token = this.token,
    expiryDate = this.expiryDate
)
