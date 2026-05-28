package com.keyfyndr.backend.features.chat.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a user's presence status.
 *
 * This is an in-memory concept — not persisted to the database.
 * When [isOnline] is true, [lastSeen] is null (user is currently active).
 * When [isOnline] is false, [lastSeen] holds the disconnect timestamp.
 */
data class UserPresence(
    val userId: UUID,
    val isOnline: Boolean,
    val lastSeen: Instant?
)
