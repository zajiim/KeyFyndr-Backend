package com.keyfyndr.backend.features.auth.domain.model

import java.util.UUID

data class DeviceToken(
    val id: UUID? = null,
    val userId: UUID,
    val deviceToken: String,
    val deviceType: String
)
