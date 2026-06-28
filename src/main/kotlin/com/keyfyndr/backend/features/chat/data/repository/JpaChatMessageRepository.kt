package com.keyfyndr.backend.features.chat.data.repository

import com.keyfyndr.backend.features.chat.data.entity.ChatMessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JPA repository for [ChatMessageEntity].
 *
 * Contains custom JPQL queries for conversation-specific operations.
 * This interface is confined to the data layer — use cases interact
 * with the domain [ChatMessageRepository] interface instead.
 */
interface JpaChatMessageRepository : JpaRepository<ChatMessageEntity, UUID> {

    /**
     * Fetches all messages between two users (in either direction), paginated.
     */
    @Query(
        """
        SELECT m FROM ChatMessageEntity m
        WHERE (m.senderId = :userId1 AND m.receiverId = :userId2)
           OR (m.senderId = :userId2 AND m.receiverId = :userId1)
        ORDER BY m.createdAt DESC
        """
    )
    fun findConversationMessages(
        @Param("userId1") userId1: UUID,
        @Param("userId2") userId2: UUID,
        pageable: Pageable
    ): Page<ChatMessageEntity>

    /**
     * Finds all distinct user IDs that the given user has exchanged messages with.
     * Returns the partner IDs for building conversation list.
     */
    @Query(
        """
        SELECT DISTINCT CASE
            WHEN m.senderId = :userId THEN m.receiverId
            ELSE m.senderId
        END
        FROM ChatMessageEntity m
        WHERE m.senderId = :userId OR m.receiverId = :userId
        """
    )
    fun findConversationPartnerIds(@Param("userId") userId: UUID): List<UUID>

    /**
     * Finds the latest message between two users.
     */
    @Query(
        """
        SELECT m FROM ChatMessageEntity m
        WHERE (m.senderId = :userId1 AND m.receiverId = :userId2)
           OR (m.senderId = :userId2 AND m.receiverId = :userId1)
        ORDER BY m.createdAt DESC
        LIMIT 1
        """
    )
    fun findLatestMessage(
        @Param("userId1") userId1: UUID,
        @Param("userId2") userId2: UUID
    ): ChatMessageEntity?

    /**
     * Counts unread messages from a specific sender to a receiver.
     */
    @Query(
        """
        SELECT COUNT(m) FROM ChatMessageEntity m
        WHERE m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false
        """
    )
    fun countUnreadFromSender(
        @Param("receiverId") receiverId: UUID,
        @Param("senderId") senderId: UUID
    ): Int

    /**
     * Counts total unread messages for a user across all conversations.
     */
    @Query(
        "SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.receiverId = :userId AND m.isRead = false"
    )
    fun countTotalUnread(@Param("userId") userId: UUID): Int

    /**
     * Fetches all unread messages from a specific sender to a receiver.
     * Used by markMessagesAsRead to get the entity list for batch saving.
     */
    @Query(
        """
        SELECT m FROM ChatMessageEntity m
        WHERE m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false
        """
    )
    fun findUnreadMessagesFromSender(
        @Param("receiverId") receiverId: UUID,
        @Param("senderId") senderId: UUID
    ): List<ChatMessageEntity>

    /**
     * Fetches messages by ID list that belong to [receiverId] and have not yet been delivered.
     * Used by markMessagesAsDelivered to find the subset actually needing an update.
     */
    @Query(
        """
        SELECT m FROM ChatMessageEntity m
        WHERE m.id IN :messageIds
          AND m.receiverId = :receiverId
          AND m.deliveredAt IS NULL
        """
    )
    fun findUndeliveredByIds(
        @Param("receiverId") receiverId: UUID,
        @Param("messageIds") messageIds: List<UUID>
    ): List<ChatMessageEntity>
}

