package com.keyfyndr.backend.features.auth.domain.repository

import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import java.util.UUID

/**
 * Domain repository interface for RefreshToken operations.
 * Use cases depend on this abstraction, not on the JPA implementation.
 */
interface RefreshTokenRepository {

    fun findByToken(token: String): RefreshToken?

    fun save(refreshToken: RefreshToken): RefreshToken

    fun delete(refreshToken: RefreshToken)

    fun deleteByUserId(userId: UUID)
}
