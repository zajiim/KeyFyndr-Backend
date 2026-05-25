package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.common.response.PageResult
import com.keyfyndr.backend.features.chat.domain.model.ChatMessage
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Retrieves paginated messages between the current user and another user.
 *
 * Results are ordered by createdAt DESC (newest first) so the client
 * can display them in reverse chronological order and load older pages.
 */
@Component
class GetConversationMessagesUseCase(
    private val chatMessageRepository: ChatMessageRepository
) {

    fun execute(currentUserId: UUID, otherUserId: UUID, page: Int, size: Int): PageResult<ChatMessage> {
        require(page >= 1) { "Page must be >= 1" }
        require(size in 1..100) { "Page size must be between 1 and 100" }

        return chatMessageRepository.findConversationMessages(currentUserId, otherUserId, page, size)
    }
}
