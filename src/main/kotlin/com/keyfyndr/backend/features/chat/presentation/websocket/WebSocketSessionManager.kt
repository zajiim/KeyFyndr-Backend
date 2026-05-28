package com.keyfyndr.backend.features.chat.presentation.websocket

import com.keyfyndr.backend.features.chat.domain.model.UserPresence
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages WebSocket sessions and user presence state.
 *
 * Thread-safe via [ConcurrentHashMap]. Tracks:
 * - Active sessions: userId → WebSocketSession
 * - Last seen timestamps: userId → Instant (set on disconnect, cleared on connect)
 *
 * Presence rules:
 * - Online: user has an active session → [lastSeen] is null
 * - Offline: user disconnected → [lastSeen] is the disconnect timestamp
 */
@Component
class WebSocketSessionManager {

    private val logger = LoggerFactory.getLogger(WebSocketSessionManager::class.java)

    /** Active WebSocket sessions indexed by user ID. */
    private val sessions = ConcurrentHashMap<UUID, WebSocketSession>()

    /** Last seen timestamps for users who have disconnected. */
    private val lastSeenMap = ConcurrentHashMap<UUID, Instant>()

    /**
     * Registers a user's WebSocket session.
     * Clears the last seen timestamp since the user is now online.
     */
    fun addSession(userId: UUID, session: WebSocketSession) {
        sessions[userId] = session
        lastSeenMap.remove(userId)
        logger.info("User $userId connected. Active sessions: ${sessions.size}")
    }

    /**
     * Removes a user's WebSocket session and records the disconnect time.
     */
    fun removeSession(userId: UUID) {
        sessions.remove(userId)
        lastSeenMap[userId] = Instant.now()
        logger.info("User $userId disconnected. Active sessions: ${sessions.size}")
    }

    /**
     * Returns the WebSocket session for a user, or null if not connected.
     */
    fun getSession(userId: UUID): WebSocketSession? = sessions[userId]

    /**
     * Returns true if the user has an active WebSocket session.
     */
    fun isOnline(userId: UUID): Boolean = sessions.containsKey(userId)

    /**
     * Returns the user's presence status.
     * When online: lastSeen is null.
     * When offline: lastSeen is the disconnect timestamp (or null if never connected).
     */
    fun getPresence(userId: UUID): UserPresence {
        val online = isOnline(userId)
        return UserPresence(
            userId = userId,
            isOnline = online,
            lastSeen = if (online) null else lastSeenMap[userId]
        )
    }

    /**
     * Returns all currently connected user IDs.
     * Used for broadcasting presence updates to relevant users.
     */
    fun getOnlineUserIds(): Set<UUID> = sessions.keys.toSet()
}
