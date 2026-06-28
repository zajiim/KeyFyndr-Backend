package com.keyfyndr.backend.features.chat.presentation.response

import java.time.Instant
import java.util.UUID

/**
 * Response DTO for a single chat message.
 * Used by both WebSocket (real-time delivery) and REST (history retrieval) endpoints.
 *
 * Delivery lifecycle visible to frontend via timestamps:
 *   SENT      → deliveredAt == null
 *   DELIVERED → deliveredAt != null && readAt == null
 *   READ      → readAt != null
 */
data class ChatMessageResponse(
    val id: UUID,
    val senderId: UUID,
    val receiverId: UUID,
    val content: String,
    val isRead: Boolean,
    val replyToId: UUID? = null,
    val replyToContent: String? = null,
    val replyToSenderId: UUID? = null,
    val createdAt: Instant,
    /** Null until the receiver's device acknowledges delivery. */
    val deliveredAt: Instant? = null,
    /** Null until the receiver marks the conversation as read. */
    val readAt: Instant? = null
)

