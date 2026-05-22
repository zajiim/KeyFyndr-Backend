package com.keyfyndr.backend.features.auth.data.service

import com.keyfyndr.backend.features.auth.domain.service.JwtService
import com.keyfyndr.backend.features.auth.security.JwtTokenProvider
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Implementation of [JwtService] backed by [JwtTokenProvider].
 *
 * Bridges the domain service interface to the infrastructure-level
 * JWT token provider which handles actual JJWT operations.
 */
@Service
class JwtServiceImpl(
    private val jwtTokenProvider: JwtTokenProvider
) : JwtService {

    override fun generateAccessToken(userId: UUID): String =
        jwtTokenProvider.generateAccessToken(userId)

    override fun generateRefreshToken(userId: UUID): String =
        jwtTokenProvider.generateRefreshToken(userId)

    override fun validateToken(token: String): Boolean =
        jwtTokenProvider.validateToken(token)

    override fun getUserIdFromToken(token: String): UUID =
        UUID.fromString(jwtTokenProvider.getUserIdFromToken(token))

    override fun getAccessTokenExpiryMs(): Long =
        jwtTokenProvider.accessTokenExpiryMs

    override fun getRefreshTokenExpiryMs(): Long =
        jwtTokenProvider.refreshTokenExpiryMs
}
