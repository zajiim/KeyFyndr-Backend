package com.keyfyndr.backend.features.chat.presentation.controller

import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.chat.domain.usecase.GetConversationMessagesUseCase
import com.keyfyndr.backend.features.chat.domain.usecase.GetUserConversationsUseCase
import com.keyfyndr.backend.features.chat.domain.usecase.MarkMessagesAsReadUseCase
import com.keyfyndr.backend.features.chat.domain.usecase.SendMessageUseCase
import com.keyfyndr.backend.features.chat.presentation.mapper.toResponse
import com.keyfyndr.backend.features.chat.presentation.request.SendMessageRequest
import com.keyfyndr.backend.features.chat.presentation.response.ChatMessageResponse
import com.keyfyndr.backend.features.chat.presentation.response.ConversationResponse
import com.keyfyndr.backend.features.chat.presentation.websocket.ChatWebSocketHandler
import com.keyfyndr.backend.features.chat.presentation.websocket.WebSocketSessionManager
import com.keyfyndr.backend.features.chat.presentation.websocket.dto.NewMessagePayload
import com.keyfyndr.backend.features.chat.presentation.websocket.dto.OutboundWebSocketMessage
import com.keyfyndr.backend.features.chat.presentation.websocket.WebSocketMessageType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for chat operations.
 *
 * Architecture decisions:
 * - No business logic in the controller — all operations delegate to use cases
 * - JWT principal is extracted as UUID from the Authentication object
 * - All responses wrapped in ApiResponse for consistency
 * - Message sending is handled via WebSocket (primary channel for real-time),
 *   this controller provides REST endpoints for history retrieval and actions
 * - Presence info (isOnline, lastSeen) is resolved from [WebSocketSessionManager]
 */
@RestController
@RequestMapping("/api/v1/chat")
class ChatRestController(
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val getUserConversationsUseCase: GetUserConversationsUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sessionManager: WebSocketSessionManager,
    private val chatWebSocketHandler: ChatWebSocketHandler
) {

    /**
     * POST /api/v1/chat/send
     * Send a message to another user via HTTP.
     * Persists the message and delivers it to the receiver over WebSocket if connected.
     * Useful for REST clients (Postman, Android) alongside the WebSocket channel.
     */
    @PostMapping("/send")
    fun sendMessage(
        @RequestBody request: SendMessageRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> {
        val senderId = extractUserId(authentication)

        val savedMessage = sendMessageUseCase.execute(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content
        )

        // Deliver to receiver over WebSocket if they are currently connected
        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.NEW_MESSAGE,
            data = NewMessagePayload(
                id = savedMessage.id!!,
                senderId = savedMessage.senderId,
                receiverId = savedMessage.receiverId,
                content = savedMessage.content,
                isRead = savedMessage.isRead,
                createdAt = savedMessage.createdAt
            )
        )
        chatWebSocketHandler.sendToUser(savedMessage.receiverId, outbound)
        chatWebSocketHandler.sendToUser(savedMessage.senderId, outbound)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Message sent successfully",
                data = savedMessage.toResponse()
            )
        )
    }

    /**
     * GET /api/v1/chat/conversations
     * List all conversations for the authenticated user.
     * Returns each conversation partner with last message preview, unread count,
     * online status, last seen, and read receipt status.
     */
    @GetMapping("/conversations")
    fun getConversations(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<ConversationResponse>>> {
        val userId = extractUserId(authentication)
        val conversations = getUserConversationsUseCase.execute(userId)

        val response = conversations.map { conversation ->
            val presence = sessionManager.getPresence(conversation.participantId)
            conversation.toResponse(
                isOnline = presence.isOnline,
                lastSeen = presence.lastSeen
            )
        }

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Conversations retrieved successfully",
                data = response
            )
        )
    }

    /**
     * GET /api/v1/chat/messages?userId=<uuid>&page=1&size=20
     * Get paginated message history between the authenticated user and another user.
     * Messages are ordered by createdAt DESC (newest first).
     */
    @GetMapping("/messages")
    fun getMessages(
        @RequestParam userId: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<ChatMessageResponse>>> {
        val currentUserId = extractUserId(authentication)
        val pageResult = getConversationMessagesUseCase.execute(currentUserId, userId, page, size)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Messages retrieved successfully",
                data = pageResult.content.map { it.toResponse() },
                pagination = pageResult.toPaginationMeta()
            )
        )
    }

    /**
     * POST /api/v1/chat/messages/read?userId=<uuid>
     * Mark all messages from a specific user as read.
     * Called when the client opens a conversation.
     */
    @PostMapping("/messages/read")
    fun markAsRead(
        @RequestParam userId: UUID,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        val currentUserId = extractUserId(authentication)
        markMessagesAsReadUseCase.execute(currentUserId, userId)

        return ResponseEntity.ok(
            ApiResponse.success(message = "Messages marked as read")
        )
    }

    /**
     * Extracts the authenticated user's UUID from the JWT principal.
     * The JwtAuthenticationFilter stores the principal as a UUID object.
     */
    private fun extractUserId(authentication: Authentication): UUID =
        when (val principal = authentication.principal) {
            is UUID -> principal
            is String -> UUID.fromString(principal)
            else -> throw IllegalStateException("Unexpected principal type: $principal")
        }
}
