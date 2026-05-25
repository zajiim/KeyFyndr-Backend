package com.keyfyndr.backend.features.chat.presentation.controller

import com.keyfyndr.backend.features.chat.domain.usecase.SendMessageUseCase
import com.keyfyndr.backend.features.chat.presentation.mapper.toResponse
import com.keyfyndr.backend.features.chat.presentation.request.SendMessageRequest
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.UUID

/**
 * STOMP WebSocket controller for real-time chat messaging.
 *
 * Architecture decisions:
 * - No business logic in the controller — delegates to [SendMessageUseCase]
 * - Authenticated user is extracted from the STOMP session principal
 *   (set by [WebSocketAuthInterceptor] during CONNECT)
 * - Messages are persisted first, then delivered to both participants
 * - Uses user-targeted queues (/user/{userId}/queue/messages) so each
 *   user only receives messages intended for them
 *
 * Client flow:
 * 1. Connect to ws://host/ws with STOMP, sending JWT in Authorization header
 * 2. Subscribe to /user/queue/messages to receive incoming messages
 * 3. Send messages to /app/chat.send with payload { receiverId, content }
 */
@Controller
class ChatWebSocketController(
    private val sendMessageUseCase: SendMessageUseCase,
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * Handles messages sent by clients to /app/chat.send
     *
     * Persists the message and delivers it in real-time to both
     * the sender (confirmation) and the receiver (if connected).
     */
    @MessageMapping("/chat.send")
    fun sendMessage(
        @Payload request: SendMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val senderId = extractUserId(headerAccessor)

        val savedMessage = sendMessageUseCase.execute(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content
        )

        val response = savedMessage.toResponse()

        // Deliver to the receiver's personal queue
        messagingTemplate.convertAndSendToUser(
            request.receiverId.toString(),
            "/queue/messages",
            response
        )

        // Also send back to the sender for confirmation/sync
        messagingTemplate.convertAndSendToUser(
            senderId.toString(),
            "/queue/messages",
            response
        )
    }

    /**
     * Extracts the authenticated user's UUID from the STOMP session principal.
     * The [WebSocketAuthInterceptor] stores the UUID as the principal during CONNECT.
     */
    private fun extractUserId(headerAccessor: SimpMessageHeaderAccessor): UUID {
        val principal = headerAccessor.user
            ?: throw IllegalStateException("No authenticated user found in WebSocket session")

        return when (val name = principal.name) {
            else -> UUID.fromString(name)
        }
    }
}
