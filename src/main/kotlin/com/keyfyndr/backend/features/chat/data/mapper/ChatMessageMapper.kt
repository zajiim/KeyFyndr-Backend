package com.keyfyndr.backend.features.chat.data.mapper

import com.keyfyndr.backend.features.chat.data.entity.ChatMessageEntity
import com.keyfyndr.backend.features.chat.domain.model.ChatMessage

/**
 * Data-layer mappers: [ChatMessageEntity] ↔ [ChatMessage] domain model.
 *
 * These mappers are confined to the data layer and handle the translation
 * between JPA entities (persistence concern) and domain models (business concern).
 *
 * The presentation layer has its own mappers (domain → response DTOs).
 */

/** Maps [ChatMessageEntity] → [ChatMessage] domain model. */
fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = this.id,
    senderId = this.senderId,
    receiverId = this.receiverId,
    content = this.content,
    isRead = this.isRead,
    replyToId = this.replyToId,
    replyToContent = this.replyToContent,
    replyToSenderId = this.replyToSenderId,
    createdAt = this.createdAt
)

/** Maps [ChatMessage] domain model → [ChatMessageEntity]. */
fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = this.id,
    senderId = this.senderId,
    receiverId = this.receiverId,
    content = this.content,
    isRead = this.isRead,
    replyToId = this.replyToId,
    replyToContent = this.replyToContent,
    replyToSenderId = this.replyToSenderId,
    createdAt = this.createdAt
)
