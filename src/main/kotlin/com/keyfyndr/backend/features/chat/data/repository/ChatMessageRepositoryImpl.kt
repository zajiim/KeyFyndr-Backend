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

            Conversation(
                participantId = partnerId,
                participantName = partnerName,
                lastMessage = latestMessage.content,
                lastMessageAt = latestMessage.createdAt,
                unreadCount = unreadCount
            )
        }.sortedByDescending { it.lastMessageAt }
    }

    @Transactional
    override fun markMessagesAsRead(receiverId: UUID, senderId: UUID) {
        jpaChatMessageRepository.markAsRead(receiverId, senderId)
    }

    override fun countUnreadMessages(userId: UUID): Int {
        return jpaChatMessageRepository.countTotalUnread(userId)
    }
}
