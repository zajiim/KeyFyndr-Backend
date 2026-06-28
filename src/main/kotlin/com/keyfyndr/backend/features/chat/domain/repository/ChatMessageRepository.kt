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

    fun findById(id: UUID): ChatMessage?

    /** Fetches paginated messages between two users, ordered by createdAt DESC. */
    fun findConversationMessages(userId1: UUID, userId2: UUID, page: Int, size: Int): PageResult<ChatMessage>

    /** Lists all conversations for a user with last message summary and unread count. */
    fun findUserConversations(userId: UUID): List<Conversation>

    /** Finds all distinct user IDs that the given user has exchanged messages with. */
    fun findConversationPartnerIds(userId: UUID): List<UUID>

    /**
     * Marks all unread messages from [senderId] to [receiverId] as read.
     *
     * Also sets deliveredAt on messages that were never acknowledged as delivered,
     * so delivery state is always consistent before read state.
     *
     * @return The UUIDs of messages that were actually updated (empty if none changed).
     */
    fun markMessagesAsRead(receiverId: UUID, senderId: UUID): List<UUID>

    /**
     * Batch-marks a set of messages as delivered for [receiverId].
     *
     * Only messages that:
     *  - are addressed to [receiverId]
     *  - have not yet been delivered (deliveredAt == null)
     * are updated. Invalid/already-delivered IDs are silently skipped (idempotent).
     *
     * @return Map of senderId → list of message IDs that were actually updated.
     *         Empty map when nothing changed.
     */
    fun markMessagesAsDelivered(receiverId: UUID, messageIds: List<UUID>): Map<UUID, List<UUID>>

    /** Counts total unread messages for a user across all conversations. */
    fun countUnreadMessages(userId: UUID): Int
}

