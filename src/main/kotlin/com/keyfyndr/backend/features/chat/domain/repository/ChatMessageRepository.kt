package com.keyfyndr.backend.features.chat.domain.repository

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.chat.domain.model.ChatMessage
import com.keyfyndr.backend.features.chat.domain.model.Conversation
import java.util.UUID

/**
 * Domain repository interface for chat message operations.
 *
 * Use cases depend on this abstraction, not on the JPA implementation.
 * The data layer provides the concrete implementation via [ChatMessageRepositoryImpl].
 */
interface ChatMessageRepository {

    fun save(message: ChatMessage): ChatMessage

    /** Fetches paginated messages between two users, ordered by createdAt DESC. */
    fun findConversationMessages(userId1: UUID, userId2: UUID, page: Int, size: Int): PageResult<ChatMessage>

    /** Lists all conversations for a user with last message summary and unread count. */
    fun findUserConversations(userId: UUID): List<Conversation>

    /** Marks all messages from [senderId] to [receiverId] as read. */
    fun markMessagesAsRead(receiverId: UUID, senderId: UUID)

    /** Counts total unread messages for a user across all conversations. */
    fun countUnreadMessages(userId: UUID): Int
}
