package com.keyfyndr.backend.features.key.presentation.mapper

import com.keyfyndr.backend.features.key.domain.model.Key
import com.keyfyndr.backend.features.key.presentation.response.KeyResponse
import com.keyfyndr.backend.features.key.presentation.response.KeyStatusResponse
import com.keyfyndr.backend.features.key.presentation.response.PublicKeyResponse

/**
 * Presentation-layer mappers: Domain model → Response DTOs.
 *
 * These are separate from the data-layer mappers (entity ↔ domain)
 * to maintain strict layer boundaries:
 *   Entity ↔ Domain (data layer)
 *   Domain → Response (presentation layer)
 */

/** Maps a domain [Key] to a full [KeyResponse] for the authenticated owner. */
fun Key.toResponse(): KeyResponse = KeyResponse(
    id = this.id.toString(),
    publicKeyId = this.publicKeyId,
    title = this.title,
    description = this.description,
    color = this.color,
    category = this.category,
    imageUrl = this.imageUrl,
    status = this.status,
    isActive = this.isActive,
    createdAt = this.createdAt
)

/** Maps a domain [Key] to a [PublicKeyResponse] for unauthenticated public lookup. */
fun Key.toPublicResponse(): PublicKeyResponse = PublicKeyResponse(
    publicKeyId = this.publicKeyId,
    title = this.title,
    description = this.description,
    color = this.color,
    category = this.category,
    imageUrl = this.imageUrl,
    status = this.status
)

/** Maps a domain [Key] to a lightweight [KeyStatusResponse] after a status change. */
fun Key.toStatusResponse(message: String): KeyStatusResponse = KeyStatusResponse(
    id = this.id.toString(),
    publicKeyId = this.publicKeyId,
    status = this.status,
    message = message
)
