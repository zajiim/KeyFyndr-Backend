package com.keyfyndr.backend.features.chat.data.repository

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.chat.data.mapper.toDomain
import com.keyfyndr.backend.features.chat.data.mapper.toEntity
import com.keyfyndr.backend.features.chat.domain.model.ChatMessage
import com.keyfyndr.backend.features.chat.domain.model.Conversation
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Data layer implementation of [ChatMessageRepository].
 *
 * Bridges the domain layer to the JPA persistence layer using mappers.
 * All JPA/entity concerns are encapsulated here — use cases only see domain models.
 *
 * Uses [UserRepository] from the auth feature to resolve participant names
 * when building conversation summaries.
 */
@Repository
class ChatMessageRepositoryImpl(
    private val jpaChatMessageRepository: JpaChatMessageRepository,
    private val userRepository: UserRepository
) : ChatMessageRepository {

    override fun save(message: ChatMessage): ChatMessage {
        val entity = message.toEntity()
        val savedEntity = jpaChatMessageRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): ChatMessage? {
        return jpaChatMessageRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun findConversationMessages(
        userId1: UUID,
        userId2: UUID,
        page: Int,
        size: Int
    ): PageResult<ChatMessage> {
        // API uses 1-indexed pages, Spring Data uses 0-indexed
        val pageable = PageRequest.of(page - 1, size)
        val springPage = jpaChatMessageRepository.findConversationMessages(userId1, userId2, pageable)

        return PageResult(
            content = springPage.content.map { it.toDomain() },
            currentPage = page,
            pageSize = size,
            totalItems = springPage.totalElements,
            totalPages = springPage.totalPages
        )
    }

    override fun findUserConversations(userId: UUID): List<Conversation> {
        val partnerIds = jpaChatMessageRepository.findConversationPartnerIds(userId)

        return partnerIds.mapNotNull { partnerId ->
            val latestMessage = jpaChatMessageRepository.findLatestMessage(userId, partnerId)
                ?: return@mapNotNull null

            val partnerUser = userRepository.findById(partnerId)
            val partnerName = partnerUser?.name ?: "Unknown User"

            val unreadCount = jpaChatMessageRepository.countUnreadFromSender(
                receiverId = userId,
                senderId = partnerId
            )

            // Determine if the last message has been read:
            // - If I sent it: check if receiver has read it (latestMessage.isRead)
            // - If I received it: it's always "read" from my perspective (I see it)
            val isLastMessageRead = if (latestMessage.senderId == userId) {
                latestMessage.isRead
            } else {
                true
            }

            Conversation(
                participantId = partnerId,
                participantName = partnerName,
                lastMessage = latestMessage.content,
                lastMessageAt = latestMessage.createdAt,
                unreadCount = unreadCount,
                isLastMessageRead = isLastMessageRead,
                lastMessageDeliveredAt = latestMessage.deliveredAt,
                lastMessageReadAt = latestMessage.readAt
            )
        }.sortedByDescending { it.lastMessageAt }
    }

    override fun findConversationPartnerIds(userId: UUID): List<UUID> {
        return jpaChatMessageRepository.findConversationPartnerIds(userId)
    }

    /**
     * Marks all unread messages from [senderId] to [receiverId] as read.
     *
     * Also backfills deliveredAt when it is missing, keeping delivery state consistent.
     * Uses saveAll for a single batch flush rather than one UPDATE per message.
     *
     * @return UUIDs of messages that were actually changed.
     */
    @Transactional
    override fun markMessagesAsRead(receiverId: UUID, senderId: UUID): List<UUID> {
        val unreadEntities = jpaChatMessageRepository.findUnreadMessagesFromSender(
            receiverId = receiverId,
            senderId = senderId
        )
        if (unreadEntities.isEmpty()) return emptyList()

        val now = Instant.now()
        unreadEntities.forEach { entity ->
            entity.isRead = true
            entity.readAt = now
            // Guard: ensure deliveredAt is always set before readAt
            if (entity.deliveredAt == null) {
                entity.deliveredAt = now
            }
        }

        jpaChatMessageRepository.saveAll(unreadEntities)
        return unreadEntities.mapNotNull { it.id }
    }

    /**
     * Batch-marks a set of messages as delivered for [receiverId].
     *
     * Only messages addressed to [receiverId] that have not yet been delivered
     * (deliveredAt == null) are updated. Already-delivered and foreign messages
     * are silently ignored (idempotent).
     *
     * Uses saveAll for a single batch flush.
     *
     * @return Map of senderId → list of message IDs that were actually updated.
     */
    @Transactional
    override fun markMessagesAsDelivered(
        receiverId: UUID,
        messageIds: List<UUID>
    ): Map<UUID, List<UUID>> {
        val entitiesToUpdate = jpaChatMessageRepository.findUndeliveredByIds(
            receiverId = receiverId,
            messageIds = messageIds
        )
        if (entitiesToUpdate.isEmpty()) return emptyMap()

        val now = Instant.now()
        entitiesToUpdate.forEach { entity ->
            entity.deliveredAt = now
        }

        jpaChatMessageRepository.saveAll(entitiesToUpdate)

        // Group updated message IDs by their original sender for targeted notification
        return entitiesToUpdate
            .groupBy { it.senderId }
            .mapValues { (_, entities) -> entities.mapNotNull { it.id } }
    }

    override fun countUnreadMessages(userId: UUID): Int {
        return jpaChatMessageRepository.countTotalUnread(userId)
    }
}
