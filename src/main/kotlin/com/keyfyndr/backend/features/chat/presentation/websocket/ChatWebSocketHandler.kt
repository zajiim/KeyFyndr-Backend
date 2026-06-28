package com.keyfyndr.backend.features.chat.presentation.websocket

import com.keyfyndr.backend.common.notification.service.FcmNotificationService
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.security.WebSocketAuthHandshakeInterceptor
import com.keyfyndr.backend.features.chat.domain.repository.ChatMessageRepository
import com.keyfyndr.backend.features.chat.domain.usecase.MarkMessagesAsDeliveredUseCase
import com.keyfyndr.backend.features.chat.domain.usecase.MarkMessagesAsReadUseCase
import com.keyfyndr.backend.features.chat.domain.usecase.SendMessageUseCase
import com.keyfyndr.backend.features.chat.presentation.websocket.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.UUID

/**
 * Plain WebSocket handler for real-time chat communication.
 *
 * Replaces the previous STOMP-based controller. All messages are JSON
 * with a "type" field for routing (see [WebSocketMessageType]).
 *
 * Responsibilities:
 * - Session lifecycle: register/unregister sessions, broadcast presence
 * - Message routing: parse inbound JSON, delegate to the appropriate handler
 * - Real-time delivery: send outbound messages to connected users
 *
 * Message delivery lifecycle:
 *   1. Sender calls SEND_MESSAGE → message persisted (SENT)
 *   2. If receiver is online:  push NEW_MESSAGE; if offline: send FCM
 *   3. Receiver's device calls MARK_DELIVERED → status = DELIVERED
 *      → DELIVERY_RECEIPT sent to original sender
 *   4. Receiver calls MARK_READ → status = READ
 *      → READ_RECEIPT sent to original sender
 *
 * Client flow:
 * 1. Connect to ws://host/ws?token=<JWT>
 * 2. Send/receive JSON messages with "type" field for routing
 */
@Component
class ChatWebSocketHandler(
    private val sessionManager: WebSocketSessionManager,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val markMessagesAsDeliveredUseCase: MarkMessagesAsDeliveredUseCase,
    private val chatMessageRepository: ChatMessageRepository,
    private val fcmNotificationService: FcmNotificationService,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(ChatWebSocketHandler::class.java)

    // ─── Session Lifecycle ──────────────────────────────────────────

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = extractUserId(session) ?: return
        sessionManager.addSession(userId, session)
        broadcastPresenceUpdate(userId, isOnline = true)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = extractUserId(session) ?: return
        sessionManager.removeSession(userId)
        broadcastPresenceUpdate(userId, isOnline = false)
    }

    // ─── Message Routing ────────────────────────────────────────────

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val userId = extractUserId(session) ?: return

        val inbound = try {
            objectMapper.readValue(message.payload, InboundWebSocketMessage::class.java)
        } catch (e: Exception) {
            logger.warn("Failed to parse WebSocket message from $userId: ${e.message}")
            sendError(session, "Invalid message format: ${e.message}")
            return
        }

        try {
            when (inbound.type.uppercase()) {
                WebSocketMessageType.SEND_MESSAGE.name    -> handleSendMessage(userId, inbound)
                WebSocketMessageType.TYPING.name          -> handleTyping(userId, inbound)
                WebSocketMessageType.MARK_READ.name       -> handleMarkRead(userId, inbound)
                WebSocketMessageType.MARK_DELIVERED.name  -> handleMarkDelivered(userId, inbound)
                else -> sendError(session, "Unknown message type: ${inbound.type}")
            }
        } catch (e: Exception) {
            logger.error("Error handling ${inbound.type} from $userId: ${e.message}", e)
            sendError(session, e.message ?: "Internal error")
        }
    }

    // ─── Inbound Message Handlers ───────────────────────────────────

    /**
     * Handles SEND_MESSAGE: persists the message and delivers to both parties.
     * Also sends a CONVERSATION_UPDATE to the receiver for their chat list.
     * FCM is only sent when the receiver has NO active WebSocket session.
     */
    private fun handleSendMessage(senderId: UUID, message: InboundWebSocketMessage) {
        val receiverId = message.receiverId
            ?: throw IllegalArgumentException("receiverId is required")
        val content = message.content
            ?: throw IllegalArgumentException("content is required")

        val savedMessage = sendMessageUseCase.execute(
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            replyToId = message.replyToId
        )

        val payload = NewMessagePayload(
            id = savedMessage.id!!,
            senderId = savedMessage.senderId,
            receiverId = savedMessage.receiverId,
            content = savedMessage.content,
            isRead = savedMessage.isRead,
            replyToId = savedMessage.replyToId,
            replyToContent = savedMessage.replyToContent,
            replyToSenderId = savedMessage.replyToSenderId,
            createdAt = savedMessage.createdAt,
            deliveredAt = savedMessage.deliveredAt,
            readAt = savedMessage.readAt
        )

        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.NEW_MESSAGE,
            data = payload
        )

        // Deliver to both sender (confirmation) and receiver (delivery)
        sendToUser(senderId, outbound)
        sendToUser(receiverId, outbound)

        // ── FCM Push Notification fallback ──────────────────────────────
        // Only send a push if the receiver has NO active WebSocket session.
        // When they are online via WS the message is already delivered above.
        val receiverSession = sessionManager.getSession(receiverId)
        if (receiverSession == null || !receiverSession.isOpen) {
            val senderName = userRepository.findById(senderId)?.name ?: "Someone"
            fcmNotificationService.sendChatMessageNotification(
                receiverId = receiverId,
                title = senderName,
                body = content,
                data = mapOf(
                    "type" to "chat_message",
                    "messageId" to savedMessage.id.toString(),
                    "senderId" to senderId.toString(),
                    "conversationId" to senderId.toString()
                )
            )
        }

        // Send conversation update to receiver for their chats list
        sendConversationUpdate(receiverId, senderId)
    }

    /**
     * Handles TYPING: forwards the typing indicator to the other user.
     */
    private fun handleTyping(senderId: UUID, message: InboundWebSocketMessage) {
        val receiverId = message.receiverId
            ?: throw IllegalArgumentException("receiverId is required")
        val isTyping = message.isTyping ?: true

        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.USER_TYPING,
            data = UserTypingPayload(
                userId = senderId,
                isTyping = isTyping
            )
        )

        sendToUser(receiverId, outbound)
    }

    /**
     * Handles MARK_DELIVERED: batch-marks the provided message IDs as delivered.
     *
     * The receiver (caller) sends a list of message IDs. For each sender whose
     * messages were actually updated, one DELIVERY_RECEIPT event is dispatched
     * containing only those message IDs. This avoids duplicate events when the
     * client retries the same acknowledgement.
     */
    private fun handleMarkDelivered(receiverId: UUID, message: InboundWebSocketMessage) {
        val messageIds = message.messageIds
        if (messageIds.isNullOrEmpty()) {
            throw IllegalArgumentException("messageIds is required and must not be empty")
        }

        // Returns map of senderId → list of actually-updated IDs.
        // Already-delivered messages are silently skipped (idempotent).
        val updatedBySender: Map<UUID, List<UUID>> =
            markMessagesAsDeliveredUseCase.execute(receiverId, messageIds)

        if (updatedBySender.isEmpty()) {
            // All messages were already delivered — nothing to notify
            return
        }

        // Notify each original sender with only the IDs belonging to them
        updatedBySender.forEach { (senderId, updatedIds) ->
            val outbound = OutboundWebSocketMessage(
                type = WebSocketMessageType.DELIVERY_RECEIPT,
                data = DeliveryReceiptPayload(messageIds = updatedIds)
            )
            sendToUser(senderId, outbound)
        }
    }

    /**
     * Handles MARK_READ: marks messages as read and notifies the original sender.
     *
     * Only messages that were not yet read are updated. If nothing changed
     * (all already read), no READ_RECEIPT event is emitted (idempotent).
     * deliveredAt is backfilled if it was never set, keeping lifecycle consistent.
     */
    private fun handleMarkRead(readerId: UUID, message: InboundWebSocketMessage) {
        val senderId = message.senderId
            ?: throw IllegalArgumentException("senderId is required")

        val updatedIds: List<UUID> = markMessagesAsReadUseCase.execute(readerId, senderId)

        // Do not emit READ_RECEIPT if nothing actually changed
        if (updatedIds.isEmpty()) return

        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.READ_RECEIPT,
            data = ReadReceiptPayload(
                readerId = readerId,
                senderId = senderId,
                messageIds = updatedIds
            )
        )

        sendToUser(senderId, outbound)
    }

    // ─── Outbound Helpers ───────────────────────────────────────────

    /**
     * Sends a JSON message to a specific user if they are connected.
     */
    fun sendToUser(userId: UUID, message: OutboundWebSocketMessage) {
        val session = sessionManager.getSession(userId)
        if (session != null && session.isOpen) {
            try {
                val json = objectMapper.writeValueAsString(message)
                session.sendMessage(TextMessage(json))
            } catch (e: Exception) {
                logger.error("Failed to send message to $userId: ${e.message}")
            }
        }
    }

    /**
     * Sends an error message to a specific session.
     */
    private fun sendError(session: WebSocketSession, errorMessage: String) {
        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.ERROR,
            data = ErrorPayload(message = errorMessage)
        )
        try {
            val json = objectMapper.writeValueAsString(outbound)
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send error message: ${e.message}")
        }
    }

    /**
     * Broadcasts a presence update to all online users who have
     * conversations with the given user.
     */
    private fun broadcastPresenceUpdate(userId: UUID, isOnline: Boolean) {
        val presence = sessionManager.getPresence(userId)
        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.PRESENCE_UPDATE,
            data = PresenceUpdatePayload(
                userId = userId,
                isOnline = presence.isOnline,
                lastSeen = presence.lastSeen
            )
        )

        // Broadcast to all connected users who have conversations with this user
        val conversationPartners = chatMessageRepository.findConversationPartnerIds(userId)
        conversationPartners.forEach { partnerId ->
            sendToUser(partnerId, outbound)
        }
    }

    /**
     * Sends a conversation update to a user (e.g., when they receive a new message
     * while on the chats list page). Includes the latest message, unread count,
     * presence info, and delivery timestamps.
     */
    private fun sendConversationUpdate(userId: UUID, otherUserId: UUID) {
        val conversations = chatMessageRepository.findUserConversations(userId)
        val conversation = conversations.firstOrNull { it.participantId == otherUserId }
            ?: return

        val presence = sessionManager.getPresence(otherUserId)

        val outbound = OutboundWebSocketMessage(
            type = WebSocketMessageType.CONVERSATION_UPDATE,
            data = ConversationUpdatePayload(
                participantId = conversation.participantId,
                participantName = conversation.participantName,
                lastMessage = conversation.lastMessage,
                lastMessageAt = conversation.lastMessageAt,
                unreadCount = conversation.unreadCount,
                isOnline = presence.isOnline,
                lastSeen = presence.lastSeen,
                isLastMessageRead = conversation.isLastMessageRead,
                lastMessageDeliveredAt = conversation.lastMessageDeliveredAt,
                lastMessageReadAt = conversation.lastMessageReadAt
            )
        )

        sendToUser(userId, outbound)
    }

    /**
     * Extracts the authenticated user's UUID from the WebSocket session attributes.
     * Set by [WebSocketAuthHandshakeInterceptor] during the HTTP upgrade handshake.
     */
    private fun extractUserId(session: WebSocketSession): UUID? {
        val userId = session.attributes[WebSocketAuthHandshakeInterceptor.USER_ID_ATTRIBUTE] as? UUID
        if (userId == null) {
            logger.error("No userId found in WebSocket session attributes")
            try {
                session.close(CloseStatus.POLICY_VIOLATION)
            } catch (e: Exception) {
                logger.error("Error closing unauthenticated session: ${e.message}")
            }
        }
        return userId
    }
}

