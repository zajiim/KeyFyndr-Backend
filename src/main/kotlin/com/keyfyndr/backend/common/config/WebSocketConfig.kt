package com.keyfyndr.backend.common.config

import com.keyfyndr.backend.features.auth.security.WebSocketAuthHandshakeInterceptor
import com.keyfyndr.backend.features.chat.presentation.websocket.ChatWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * Plain WebSocket configuration (no STOMP).
 *
 * Registers the [ChatWebSocketHandler] at the /ws endpoint with JWT
 * authentication via [WebSocketAuthHandshakeInterceptor].
 *
 * Client connection:
 * ```
 * ws://host/ws?token=<JWT>
 * ```
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val webSocketAuthHandshakeInterceptor: WebSocketAuthHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/ws")
            .addInterceptors(webSocketAuthHandshakeInterceptor)
            .setAllowedOriginPatterns("*")
    }
}
