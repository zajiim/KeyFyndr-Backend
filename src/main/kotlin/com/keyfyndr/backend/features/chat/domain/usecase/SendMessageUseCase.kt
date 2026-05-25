package com.keyfyndr.backend.features.chat.domain.usecase

import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.chat.domain.model.ChatMessage
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Sends a chat message from one user to another.
 *
 * Responsibilities:
 * - Validate sender ≠ receiver
 * - Validate receiver exists
 * - Persist the message via [ChatMessageRepository]
 */
@Component
class SendMessageUseCase(
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository
) {

    fun execute(senderId: UUID, receiverId: UUID, content: String): ChatMessage {
        require(senderId != receiverId) { "Cannot send a message to yourself" }
        require(content.isNotBlank()) { "Message content cannot be blank" }

        userRepository.findById(receiverId)
            ?: throw ResourceNotFoundException("Receiver not found")

        val message = ChatMessage(
            senderId = senderId,
            receiverId = receiverId,
            content = content.trim()
        )

        return chatMessageRepository.save(message)
    }
}
