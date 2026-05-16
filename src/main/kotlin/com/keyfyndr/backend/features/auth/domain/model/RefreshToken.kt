package com.keyfyndr.backend.features.auth.domain.model

import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID? = null,
    val userId: UUID,
    val token: String,
    val expiryDate: Instant
)
