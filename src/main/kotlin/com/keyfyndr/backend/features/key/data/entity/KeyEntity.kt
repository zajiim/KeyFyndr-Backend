package com.keyfyndr.backend.features.key.data.entity

import com.keyfyndr.backend.features.key.domain.enums.KeyStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * JPA entity mapped to the "keys" table.
 *
 * Architecture note: This entity is confined to the data layer.
 * It is never exposed to use cases or the presentation layer directly —
 * [KeyMapper] handles conversion to/from the domain [Key] model.
 *
 * Design decision: We store owner_id as a direct UUID column rather than using
 * @ManyToOne to UserEntity. This avoids dual-column mapping issues and keeps
 * the key feature decoupled from the auth feature's entity internals.
 * The FK constraint is enforced at the database level via the Flyway migration.
 */
@Entity
@Table(name = "keys")
class KeyEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "public_key_id", nullable = false, unique = true, length = 10)
    val publicKeyId: String = "",

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 50)
    var color: String? = null,

    @Column(nullable = false, length = 100)
    var category: String = "",

    @Column(name = "image_url", columnDefinition = "TEXT")
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: KeyStatus = KeyStatus.SAFE,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    /**
     * FK to users table — stored as a direct UUID column.
     * The foreign key constraint is enforced at DB level (Flyway migration).
     */
    @Column(name = "owner_id", nullable = false)
    val ownerId: UUID = UUID.randomUUID(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Location coordinates captured when the key is reported LOST or FOUND.
     * Null for newly created keys or if the user didn't provide location.
     */
    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,

    /**
     * Timestamp of the last status transition (e.g., SAFE→LOST).
     * Used by the Home Dashboard for recency-based sorting of nearby activities.
     */
    @Column(name = "last_status_update_at")
    var lastStatusUpdateAt: Instant? = null
)
