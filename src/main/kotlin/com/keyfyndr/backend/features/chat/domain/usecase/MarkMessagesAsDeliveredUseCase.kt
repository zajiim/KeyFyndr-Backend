package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Marks a set of messages as delivered for the current user (receiver).
 *
 * Responsibilities:
 * - Filter out IDs that are already delivered or don't belong to the receiver
 * - Batch update deliveredAt in a single transaction
 * - Return a map of senderId → updatedMessageIds so the caller can notify each sender
 *
 * Idempotent: calling this multiple times with the same IDs is safe — already-delivered
 * messages are silently skipped, and no duplicate events are emitted.
 */
@Component
class MarkMessagesAsDeliveredUseCase(
    private val chatMessageRepository: ChatMessageRepository
) {

    /**
     * @param receiverId  The user acknowledging delivery (must be the message receiver).
     * @param messageIds  Arbitrary list of message IDs to mark as delivered.
     * @return Map of senderId → list of message IDs that were actually updated.
     *         Empty map when nothing changed (all already delivered or invalid IDs).
     */
    fun execute(receiverId: UUID, messageIds: List<UUID>): Map<UUID, List<UUID>> {
        if (messageIds.isEmpty()) return emptyMap()

        return chatMessageRepository.markMessagesAsDelivered(
            receiverId = receiverId,
            messageIds = messageIds
        )
    }
}
