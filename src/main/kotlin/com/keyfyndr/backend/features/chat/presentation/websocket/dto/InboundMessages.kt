package com.keyfyndr.backend.features.chat.presentation.websocket.dto

import java.util.UUID

/**
 * Wrapper for all inbound WebSocket messages from the client.
 *
 * The [type] field determines which handler processes the message.
 * Only the fields relevant to that type need to be populated —
 * the handler validates required fields at runtime.
 *
 * Example payloads:
 * ```json
 * { "type": "SEND_MESSAGE", "receiverId": "uuid", "content": "hello" }
 * { "type": "TYPING", "receiverId": "uuid", "isTyping": true }
 * { "type": "MARK_READ", "senderId": "uuid" }
 * ```
 */
data class InboundWebSocketMessage(
    val type: String,
    val receiverId: UUID? = null,
    val senderId: UUID? = null,
    val content: String? = null,
    val isTyping: Boolean? = null,
    val replyToId: UUID? = null
)
