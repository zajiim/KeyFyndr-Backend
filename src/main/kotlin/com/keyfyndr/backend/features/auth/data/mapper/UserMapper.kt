package com.keyfyndr.backend.features.auth.data.mapper

import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.data.entity.UserEntity

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = this.name,
    email = this.email,
    phone = this.phone,
    passwordHash = this.passwordHash,
    verified = this.verified,
    createdAt = this.createdAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    email = this.email,
    phone = this.phone,
    passwordHash = this.passwordHash,
    verified = this.verified,
    createdAt = this.createdAt
)
