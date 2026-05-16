package com.keyfyndr.backend.features.auth.data.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var token: String = "",

    @Column(name = "expiry_date", nullable = false)
    var expiryDate: Instant = Instant.now()
)
