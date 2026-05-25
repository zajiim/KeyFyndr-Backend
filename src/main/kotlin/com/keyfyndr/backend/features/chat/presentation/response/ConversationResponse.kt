package com.keyfyndr.backend.features.chat.presentation.response

import java.time.Instant
import java.util.UUID

/**
 * Response DTO for a conversation summary in the conversations list.
 * Contains the other participant's info, last message preview, and unread count.
 */
data class ConversationResponse(
    val participantId: UUID,
    val participantName: String,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val unreadCount: Int
)
