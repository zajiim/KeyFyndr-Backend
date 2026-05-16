package com.keyfyndr.backend.features.auth.service

import com.keyfyndr.backend.features.auth.security.JwtTokenProvider
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class JwtService(
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun generateAccessToken(userId: UUID): String =
        jwtTokenProvider.generateAccessToken(userId)

    fun generateRefreshToken(userId: UUID): String =
        jwtTokenProvider.generateRefreshToken(userId)

    fun validateToken(token: String): Boolean =
        jwtTokenProvider.validateToken(token)

    fun getUserIdFromToken(token: String): UUID =
        UUID.fromString(jwtTokenProvider.getUserIdFromToken(token))

    fun getAccessTokenExpiryMs(): Long =
        jwtTokenProvider.accessTokenExpiryMs

    fun getRefreshTokenExpiryMs(): Long =
        jwtTokenProvider.refreshTokenExpiryMs
}
