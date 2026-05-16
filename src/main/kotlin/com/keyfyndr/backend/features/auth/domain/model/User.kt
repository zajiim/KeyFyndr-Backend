package com.keyfyndr.backend.features.auth.domain.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID? = null,
    val name: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val verified: Boolean = false,
    val createdAt: Instant = Instant.now()
)
