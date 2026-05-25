package com.keyfyndr.backend.features.chat.infrastructure

import com.keyfyndr.backend.features.auth.domain.service.JwtService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Intercepts STOMP CONNECT frames to authenticate the user via JWT.
 *
 * The client must send the JWT token in the STOMP native header "Authorization"
 * with the format "Bearer <token>" when establishing the connection:
 *
 * ```
 * CONNECT
 * Authorization:Bearer eyJhbGciOiJI...
 * ```
 *
 * On successful validation, the user's UUID is stored as the principal
 * on the STOMP session. This enables:
 * - @MessageMapping handlers to access the authenticated user
 * - User-targeted message delivery via /user/{userId}/queue/...
 *
 * If the token is missing or invalid, the connection is rejected.
 */
@Component
class WebSocketAuthInterceptor(
    private val jwtService: JwtService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)

                if (jwtService.validateToken(token)) {
                    val userId = jwtService.getUserIdFromToken(token)

                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,     // principal — the user's UUID
                        null,       // credentials
                        emptyList() // authorities
                    )

                    accessor.user = authentication
                } else {
                    throw IllegalArgumentException("Invalid or expired JWT token")
                }
            } else {
                throw IllegalArgumentException("Missing Authorization header in STOMP CONNECT")
            }
        }

        return message
    }
}
