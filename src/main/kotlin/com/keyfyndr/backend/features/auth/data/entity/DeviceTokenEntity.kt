package com.keyfyndr.backend.features.auth.data.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "device_tokens")
class DeviceTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(name = "device_token", nullable = false)
    var deviceToken: String = "",

    @Column(name = "device_type", nullable = false)
    var deviceType: String = ""
)
