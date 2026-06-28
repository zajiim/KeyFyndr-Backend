package com.keyfyndr.backend.features.chat.presentation.websocket

/**
 * Defines the message types used in the WebSocket JSON protocol.
 *
 * Client → Server (inbound):
 * - [SEND_MESSAGE]    — send a chat message to another user
 * - [TYPING]          — notify typing status to the other user
 * - [MARK_READ]       — mark messages from a sender as read
 * - [MARK_DELIVERED]  — acknowledge delivery of one or more messages (batch)
 *
 * Server → Client (outbound):
 * - [NEW_MESSAGE]          — a new chat message has been received/confirmed
 * - [USER_TYPING]          — another user is typing
 * - [PRESENCE_UPDATE]      — a user came online or went offline
 * - [READ_RECEIPT]         — messages have been marked as read by the receiver
 * - [DELIVERY_RECEIPT]     — messages have been delivered to the receiver's device
 * - [CONVERSATION_UPDATE]  — a conversation's latest message has changed
 * - [ERROR]                — an error occurred processing a client message
 */
enum class WebSocketMessageType {
    // Client → Server
    SEND_MESSAGE,
    TYPING,
    MARK_READ,
    MARK_DELIVERED,

    // Server → Client
    NEW_MESSAGE,
    USER_TYPING,
    PRESENCE_UPDATE,
    READ_RECEIPT,
    DELIVERY_RECEIPT,
    CONVERSATION_UPDATE,
    ERROR
}

