package com.keyfyndr.backend.features.chat.presentation.response

import java.time.Instant
import java.util.UUID

/**
 * Response DTO for a conversation summary in the conversations list.
 * Contains the other participant's info, last message preview, unread count,
 * online status, last seen timestamp, and whether the last message was read.
 */
data class ConversationResponse(
    val participantId: UUID,
    val participantName: String,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val unreadCount: Int,
    val isOnline: Boolean,
    val lastSeen: Instant?,
    val isLastMessageRead: Boolean
)

