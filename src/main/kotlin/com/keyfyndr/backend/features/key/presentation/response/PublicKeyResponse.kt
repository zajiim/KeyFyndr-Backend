package com.keyfyndr.backend.features.key.presentation.response

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus

/**
 * Public-safe key response returned by the unauthenticated public lookup endpoint.
 *
 * Deliberately excludes sensitive/internal fields:
 * - Internal UUID (id)
 * - Owner identity
 * - isActive flag
 * - Timestamps
 *
 * This response is designed for crowd-based sighting scenarios where someone
 * scans/finds a key and looks up its public ID to see if it's reported lost.
 */
data class PublicKeyResponse(
    val publicKeyId: String,
    val title: String,
    val description: String?,
    val color: String?,
    val category: String,
    val imageUrl: String?,
    val status: KeyStatus
)
