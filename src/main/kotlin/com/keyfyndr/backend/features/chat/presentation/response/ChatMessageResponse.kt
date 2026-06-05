package com.keyfyndr.backend.features.chat.presentation.response

import java.time.Instant
import java.util.UUID

/**
 * Response DTO for a single chat message.
 * Used by both WebSocket (real-time delivery) and REST (history retrieval) endpoints.
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
    val createdAt: Instant
)
