package com.keyfyndr.backend.features.key.data.mapper

import com.keyfyndr.backend.features.key.data.entity.KeyEntity
import com.keyfyndr.backend.features.key.domain.model.Key

/**
 * Data-layer mappers: [KeyEntity] ↔ [Key] domain model.
 *
 * These mappers are confined to the data layer and handle the translation
 * between JPA entities (persistence concern) and domain models (business concern).
 *
 * The presentation layer has its own mappers (domain → response DTOs).
 */

/** Maps [KeyEntity] → [Key] domain model. */
fun KeyEntity.toDomain(): Key = Key(
    id = this.id,
    publicKeyId = this.publicKeyId,
    title = this.title,
    description = this.description,
    color = this.color,
    category = this.category,
    imageUrl = this.imageUrl,
    status = this.status,
    isActive = this.isActive,
    ownerId = this.ownerId,
    createdAt = this.createdAt,
    latitude = this.latitude,
    longitude = this.longitude,
    lastStatusUpdateAt = this.lastStatusUpdateAt
)

/**
 * Maps [Key] domain model → [KeyEntity].
 *
 * Uses the flat ownerId column directly — no need to create a UserEntity proxy.
 * The @ManyToOne relationship on KeyEntity is read-only (insertable/updatable = false).
 */
fun Key.toEntity(): KeyEntity = KeyEntity(
    id = this.id,
    publicKeyId = this.publicKeyId,
    title = this.title,
    description = this.description,
    color = this.color,
    category = this.category,
    imageUrl = this.imageUrl,
    status = this.status,
    isActive = this.isActive,
    ownerId = this.ownerId,
    createdAt = this.createdAt,
    latitude = this.latitude,
    longitude = this.longitude,
    lastStatusUpdateAt = this.lastStatusUpdateAt
)
