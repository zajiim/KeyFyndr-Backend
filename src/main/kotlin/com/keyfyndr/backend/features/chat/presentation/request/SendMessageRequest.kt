package com.keyfyndr.backend.features.chat.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Request payload for sending a chat message.
 * Used by both the WebSocket controller (STOMP) and potentially REST fallback.
 */
data class SendMessageRequest(

    @field:NotNull(message = "Receiver ID is required")
    val receiverId: UUID,

    @field:NotBlank(message = "Message content cannot be blank")
    val content: String
)
