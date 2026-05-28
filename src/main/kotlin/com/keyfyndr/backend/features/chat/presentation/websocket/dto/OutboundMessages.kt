package com.keyfyndr.backend.features.chat.presentation.websocket.dto

import com.keyfyndr.backend.features.chat.presentation.websocket.WebSocketMessageType
import java.time.Instant
import java.util.UUID

/**
 * Wrapper for all outbound WebSocket messages sent to clients.
 *
 * Every message includes a [type] field so the client can route
 * it to the appropriate handler. The [data] field contains the
 * type-specific payload.
 */
data class OutboundWebSocketMessage(
    val type: WebSocketMessageType,
    val data: Any? = null
)

/**
 * Payload for [WebSocketMessageType.NEW_MESSAGE].
 * Sent to both sender (as confirmation) and receiver (as delivery).
 */
data class NewMessagePayload(
    val id: UUID,
    val senderId: UUID,
    val receiverId: UUID,
    val content: String,
    val isRead: Boolean,
    val createdAt: Instant
)

/**
 * Payload for [WebSocketMessageType.USER_TYPING].
 * Sent to the other participant in a conversation.
 */
data class UserTypingPayload(
    val userId: UUID,
    val isTyping: Boolean
)

/**
 * Payload for [WebSocketMessageType.PRESENCE_UPDATE].
 * Broadcast to users who have conversations with the affected user.
 *
 * When [isOnline] is true, [lastSeen] is null — the user is currently active.
 * When [isOnline] is false, [lastSeen] is set to the disconnect timestamp.
 */
data class PresenceUpdatePayload(
    val userId: UUID,
    val isOnline: Boolean,
    val lastSeen: Instant?
)

/**
 * Payload for [WebSocketMessageType.READ_RECEIPT].
 * Sent to the original sender when the receiver marks messages as read.
 */
data class ReadReceiptPayload(
    val readerId: UUID,
    val senderId: UUID
)

/**
 * Payload for [WebSocketMessageType.CONVERSATION_UPDATE].
 * Sent when a new message arrives while the user is on the conversations list page.
 */
data class ConversationUpdatePayload(
    val participantId: UUID,
    val participantName: String,
    val lastMessage: String,
    val lastMessageAt: Instant,
    val unreadCount: Int,
    val isOnline: Boolean,
    val lastSeen: Instant?,
    val isLastMessageRead: Boolean
)

/**
 * Payload for [WebSocketMessageType.ERROR].
 * Sent when a client message cannot be processed.
 */
data class ErrorPayload(
    val message: String
)
