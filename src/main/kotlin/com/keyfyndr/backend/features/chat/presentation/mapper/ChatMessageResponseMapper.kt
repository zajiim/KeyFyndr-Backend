package com.keyfyndr.backend.features.chat.presentation.mapper

import com.keyfyndr.backend.features.chat.domain.model.ChatMessage
import com.keyfyndr.backend.features.chat.domain.model.Conversation
import com.keyfyndr.backend.features.chat.presentation.response.ChatMessageResponse
import com.keyfyndr.backend.features.chat.presentation.response.ConversationResponse
import java.time.Instant

/**
 * Presentation-layer mappers: domain models → response DTOs.
 *
 * These mappers are confined to the presentation layer and handle the
 * translation from domain models to API response DTOs.
 */

/** Maps [ChatMessage] domain model → [ChatMessageResponse] DTO. */
fun ChatMessage.toResponse(): ChatMessageResponse = ChatMessageResponse(
    id = this.id!!,
    senderId = this.senderId,
    receiverId = this.receiverId,
    content = this.content,
    isRead = this.isRead,
    createdAt = this.createdAt
)

/**
 * Maps [Conversation] domain model → [ConversationResponse] DTO.
 *
 * Presence info ([isOnline], [lastSeen]) is provided by the caller
 * since it comes from the in-memory [WebSocketSessionManager],
 * not from the domain model.
 */
fun Conversation.toResponse(
    isOnline: Boolean,
    lastSeen: Instant?
): ConversationResponse = ConversationResponse(
    participantId = this.participantId,
    participantName = this.participantName,
    lastMessage = this.lastMessage,
    lastMessageAt = this.lastMessageAt,
    unreadCount = this.unreadCount,
    isOnline = isOnline,
    lastSeen = lastSeen,
    isLastMessageRead = this.isLastMessageRead
)

