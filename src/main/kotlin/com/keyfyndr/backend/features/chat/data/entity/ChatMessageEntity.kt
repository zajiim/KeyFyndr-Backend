package com.keyfyndr.backend.features.chat.data.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * JPA entity mapped to the "chat_messages" table.
 *
 * Architecture note: This entity is confined to the data layer.
 * It is never exposed to use cases or the presentation layer directly —
 * [ChatMessageMapper] handles conversion to/from the domain [ChatMessage] model.
 *
 * Design decision: We store sender_id and receiver_id as direct UUID columns
 * rather than using @ManyToOne to UserEntity. This keeps the chat feature
 * decoupled from the auth feature's entity internals, consistent with how
 * KeyEntity handles owner_id.
 */
@Entity
@Table(name = "chat_messages")
class ChatMessageEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "sender_id", nullable = false)
    val senderId: UUID = UUID.randomUUID(),

    @Column(name = "receiver_id", nullable = false)
    val receiverId: UUID = UUID.randomUUID(),

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String = "",

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
