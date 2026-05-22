package com.keyfyndr.backend.features.key.presentation.response

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import java.time.Instant

/**
 * Full key response returned to the authenticated owner.
 * Contains all key details including ownership and timestamps.
 */
data class KeyResponse(
    val id: String,
    val publicKeyId: String,
    val title: String,
    val description: String?,
    val color: String?,
    val category: String,
    val imageUrl: String?,
    val status: KeyStatus,
    val isActive: Boolean,
    val createdAt: Instant
)
