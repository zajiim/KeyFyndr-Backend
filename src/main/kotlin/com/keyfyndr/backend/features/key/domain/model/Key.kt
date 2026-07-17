package com.keyfyndr.backend.features.key.domain.model

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a physical key tracked in the system.
 *
 * This is a pure domain object — it has no JPA annotations or framework dependencies.
 * The data layer maps between this model and the JPA entity.
 */
data class Key(
    val id: UUID? = null,
    val publicKeyId: String,
    val title: String,
    val description: String? = null,
    val color: String? = null,
    val category: String,
    val imageUrl: String? = null,
    val status: KeyStatus = KeyStatus.SAFE,
    val isActive: Boolean = true,
    val ownerId: UUID,
    val createdAt: Instant = Instant.now(),

    /**
     * Location coordinates captured when the key is reported LOST or FOUND.
     * Null when the key is first created (status = SAFE) or if location was not provided.
     */
    val latitude: Double? = null,
    val longitude: Double? = null,

    /**
     * Timestamp of the last status change (SAFE→LOST, LOST→FOUND, etc.).
     * Used by the Home Dashboard to sort nearby activities by recency.
     */
    val lastStatusUpdateAt: Instant? = null
)
