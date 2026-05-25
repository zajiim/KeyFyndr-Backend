package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Marks all messages from a specific sender as read for the current user.
 *
 * Called when a user opens a conversation to indicate they've seen the messages.
 */
@Component
class MarkMessagesAsReadUseCase(
    private val chatMessageRepository: ChatMessageRepository
) {

    fun execute(currentUserId: UUID, senderId: UUID) {
        chatMessageRepository.markMessagesAsRead(
            receiverId = currentUserId,
            senderId = senderId
        )
    }
}
