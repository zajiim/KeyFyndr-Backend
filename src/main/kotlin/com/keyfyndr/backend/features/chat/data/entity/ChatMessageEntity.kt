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
 *
 * Delivery lifecycle (via nullable timestamps):
 *   SENT      → deliveredAt == null
 *   DELIVERED → deliveredAt != null && readAt == null
 *   READ      → readAt != null
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

    @Column(name = "reply_to_id")
    val replyToId: UUID? = null,

    @Column(name = "reply_to_content", columnDefinition = "TEXT")
    val replyToContent: String? = null,

    @Column(name = "reply_to_sender_id")
    val replyToSenderId: UUID? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /** Set when the receiver's device acknowledges delivery. */
    @Column(name = "delivered_at")
    var deliveredAt: Instant? = null,

    /** Set when the receiver opens and reads the conversation. */
    @Column(name = "read_at")
    var readAt: Instant? = null
)

