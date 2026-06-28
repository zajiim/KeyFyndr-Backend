package com.keyfyndr.backend.features.chat.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a conversation summary for listing purposes.
 *
 * A conversation is derived from the messages between two users — there is no
 * separate conversations table. This model aggregates the latest message and
 * unread count for display in a conversation list.
 */
data class Conversation(
    val participantId: UUID,
    val participantName: String,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val unreadCount: Int,
    val isLastMessageRead: Boolean,
    /** deliveredAt of the last message — null means still in SENT state. */
    val lastMessageDeliveredAt: Instant? = null,
    /** readAt of the last message — null means not yet read. */
    val lastMessageReadAt: Instant? = null
)

