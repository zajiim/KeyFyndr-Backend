package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Marks all messages from a specific sender as read for the current user.
 *
 * Called when a user opens a conversation to indicate they've seen the messages.
 * Also backfills deliveredAt if it was never set (keeps lifecycle consistent).
 *
 * @return UUIDs of messages that were actually updated. Empty list if all were
 *         already read (idempotent — no duplicate READ_RECEIPT events should be sent).
 */
@Component
class MarkMessagesAsReadUseCase(
    private val chatMessageRepository: ChatMessageRepository
) {

    fun execute(currentUserId: UUID, senderId: UUID): List<UUID> {
        return chatMessageRepository.markMessagesAsRead(
            receiverId = currentUserId,
            senderId = senderId
        )
    }
}

