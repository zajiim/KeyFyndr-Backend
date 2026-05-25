package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.features.chat.domain.model.Conversation
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Lists all conversations for the authenticated user.
 *
 * Each conversation includes the other participant's info,
 * the last message text and timestamp, and the unread message count.
 * Results are ordered by lastMessageAt DESC (most recent first).
 */
@Component
class GetUserConversationsUseCase(
    private val chatMessageRepository: ChatMessageRepository
) {

    fun execute(userId: UUID): List<Conversation> {
        return chatMessageRepository.findUserConversations(userId)
    }
}
