package com.keyfyndr.backend.features.auth.domain.service

import java.util.UUID

/**
 * Domain service interface for JWT token operations.
 *
 * Use cases depend on this abstraction, not on the JJWT-backed implementation.
 * The data layer provides the concrete implementation via [JwtServiceImpl].
 */
interface JwtService {

    fun generateAccessToken(userId: UUID): String

    fun generateRefreshToken(userId: UUID): String

    fun validateToken(token: String): Boolean

    fun getUserIdFromToken(token: String): UUID

    fun getAccessTokenExpiryMs(): Long

    fun getRefreshTokenExpiryMs(): Long
}
