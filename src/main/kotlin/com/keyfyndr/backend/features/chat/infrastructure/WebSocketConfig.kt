package com.keyfyndr.backend.features.chat.infrastructure

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * WebSocket configuration using STOMP protocol.
 *
 * Architecture decisions:
 * - Uses an in-memory simple broker (sufficient for single-instance deployments)
 * - STOMP endpoint at /ws — clients connect here to establish WebSocket
 * - Application destination prefix /app — clients send messages to /app/chat.send
 * - User destination prefix /user — for targeted delivery to specific users
 * - Authentication is handled at the STOMP CONNECT frame level via [WebSocketAuthInterceptor]
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val webSocketAuthInterceptor: WebSocketAuthInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enable simple in-memory broker for user-targeted queues
        config.enableSimpleBroker("/queue")
        // Prefix for messages from client → server (handled by @MessageMapping)
        config.setApplicationDestinationPrefixes("/app")
        // Prefix for user-targeted messages (e.g., /user/{userId}/queue/messages)
        config.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        // Register the JWT auth interceptor on the inbound channel
        // This intercepts STOMP CONNECT frames to authenticate the user
        registration.interceptors(webSocketAuthInterceptor)
    }
}
