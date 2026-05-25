package com.keyfyndr.backend.features.chat.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a single chat message between two users.
 *
 * Architecture note: This is a pure domain object with no JPA or framework
 * annotations. The data layer handles mapping to/from [ChatMessageEntity].
 */
data class ChatMessage(
    val id: UUID? = null,
    val senderId: UUID,
    val receiverId: UUID,
    val content: String,
    val isRead: Boolean = false,
    val createdAt: Instant = Instant.now()
)
