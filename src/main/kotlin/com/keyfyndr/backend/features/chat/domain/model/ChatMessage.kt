package com.keyfyndr.backend.features.chat.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a single chat message between two users.
 *
 * Delivery lifecycle tracked via timestamps (preferred over booleans because
 * they carry both the state and when it changed):
 *   SENT      → deliveredAt == null && readAt == null
 *   DELIVERED → deliveredAt != null && readAt == null
 *   READ      → readAt != null (deliveredAt also set as a guard)
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
    val replyToId: UUID? = null,
    val replyToContent: String? = null,
    val replyToSenderId: UUID? = null,
    val createdAt: Instant = Instant.now(),
    /** Set when the receiver's device acknowledges receipt. Null = SENT state. */
    val deliveredAt: Instant? = null,
    /** Set when the receiver marks the conversation as read. Null = not yet read. */
    val readAt: Instant? = null
)
