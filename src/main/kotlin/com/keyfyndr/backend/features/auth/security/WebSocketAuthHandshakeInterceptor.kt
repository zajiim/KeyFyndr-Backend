package com.keyfyndr.backend.features.auth.security

import com.keyfyndr.backend.features.auth.domain.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

/**
 * Authenticates WebSocket connections during the HTTP upgrade handshake.
 *
 * The client must provide the JWT token as a query parameter:
 * ```
 * ws://host/ws?token=<JWT>
 * ```
 *
 * On successful validation, the user's UUID is stored in the WebSocket
 * session attributes under key "userId". The [ChatWebSocketHandler] reads
 * this attribute to identify the authenticated user.
 *
 * If the token is missing or invalid, the handshake is rejected (returns false).
 */
@Component
class WebSocketAuthHandshakeInterceptor(
    private val jwtService: JwtService
) : HandshakeInterceptor {

    private val logger = LoggerFactory.getLogger(WebSocketAuthHandshakeInterceptor::class.java)

    companion object {
        /** Key used to store the authenticated user's UUID in session attributes. */
        const val USER_ID_ATTRIBUTE = "userId"
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = extractToken(request)

        if (token == null) {
            logger.warn("WebSocket handshake rejected: missing token parameter")
            return false
        }

        if (!jwtService.validateToken(token)) {
            logger.warn("WebSocket handshake rejected: invalid or expired token")
            return false
        }

        val userId = jwtService.getUserIdFromToken(token)
        attributes[USER_ID_ATTRIBUTE] = userId
        logger.debug("WebSocket handshake authenticated for user $userId")

        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // No-op: nothing needed after handshake
    }

    /**
     * Extracts the JWT token from the "token" query parameter.
     * Supports both `?token=<jwt>` format.
     */
    private fun extractToken(request: ServerHttpRequest): String? {
        if (request is ServletServerHttpRequest) {
            return request.servletRequest.getParameter("token")
        }
        // Fallback: parse from URI query string
        val query = request.uri.query ?: return null
        return query.split("&")
            .map { it.split("=", limit = 2) }
            .firstOrNull { it[0] == "token" }
            ?.getOrNull(1)
    }
}
