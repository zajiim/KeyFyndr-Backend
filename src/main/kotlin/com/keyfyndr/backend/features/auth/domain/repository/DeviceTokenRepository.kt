package com.keyfyndr.backend.features.auth.domain.repository

import com.keyfyndr.backend.features.auth.domain.model.DeviceToken
import java.util.UUID

/**
 * Domain repository interface for DeviceToken operations.
 * Use cases depend on this abstraction, not on the JPA implementation.
 */
interface DeviceTokenRepository {

    /** Persist or update a device token for a user. */
    fun save(deviceToken: DeviceToken): DeviceToken

    /** Find all tokens registered for a specific user (all devices). */
    fun findByUserId(userId: UUID): List<DeviceToken>

    /** Remove a specific token by its FCM registration string. */
    fun deleteByToken(token: String)

    /** Remove all tokens belonging to a user (used on logout). */
    fun deleteByUserId(userId: UUID)

    /** Check if a token string already exists (to avoid duplicates). */
    fun existsByDeviceToken(token: String): Boolean

    /** Delete specific invalid/stale tokens. */
    fun deleteByTokens(tokens: List<String>)
}
