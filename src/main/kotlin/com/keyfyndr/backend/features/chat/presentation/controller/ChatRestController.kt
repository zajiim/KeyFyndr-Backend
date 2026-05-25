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
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
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
 * - The send endpoint persists the message AND delivers it via WebSocket
 *   to any connected recipients, providing both REST and real-time support
 */
@RestController
@RequestMapping("/api/v1/chat")
class ChatRestController(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val getUserConversationsUseCase: GetUserConversationsUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * POST /api/v1/chat/send
     * Send a chat message via REST. The message is persisted and also
     * delivered in real-time to connected WebSocket clients.
     *
     * This serves as a REST alternative to the WebSocket /app/chat.send endpoint,
     * useful for testing and for clients not connected via WebSocket.
     */
    @PostMapping("/send")
    fun sendMessage(
        @Valid @RequestBody request: SendMessageRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> {
        val senderId = extractUserId(authentication)

        val savedMessage = sendMessageUseCase.execute(
            senderId = senderId,
            receiverId = request.receiverId,
            content = request.content
        )

        val response = savedMessage.toResponse()

        // Also deliver via WebSocket to connected clients
        messagingTemplate.convertAndSendToUser(
            request.receiverId.toString(),
            "/queue/messages",
            response
        )
        messagingTemplate.convertAndSendToUser(
            senderId.toString(),
            "/queue/messages",
            response
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                statusCode = HttpStatus.CREATED.value(),
                message = "Message sent successfully",
                data = response
            )
        )
    }

    /**
     * GET /api/v1/chat/conversations
     * List all conversations for the authenticated user.
     * Returns each conversation partner with last message preview and unread count.
     */
    @GetMapping("/conversations")
    fun getConversations(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<ConversationResponse>>> {
        val userId = extractUserId(authentication)
        val conversations = getUserConversationsUseCase.execute(userId)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Conversations retrieved successfully",
                data = conversations.map { it.toResponse() }
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
