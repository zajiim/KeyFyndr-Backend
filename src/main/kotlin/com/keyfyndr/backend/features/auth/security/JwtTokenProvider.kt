package com.keyfyndr.backend.features.auth.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}")
    private val jwtSecret: String,

    @Value("\${app.jwt.access-token-expiry-ms}")
    val accessTokenExpiryMs: Long,

    @Value("\${app.jwt.refresh-token-expiry-ms}")
    val refreshTokenExpiryMs: Long
) {

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateAccessToken(userId: UUID): String =
        buildToken(userId, accessTokenExpiryMs)

    fun generateRefreshToken(userId: UUID): String =
        buildToken(userId, refreshTokenExpiryMs)

    fun validateToken(token: String): Boolean =
        try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (ex: Exception) {
            false
        }

    fun getUserIdFromToken(token: String): String =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    private fun buildToken(userId: UUID, expiryMs: Long): String {
        val now = Date()
        val expiry = Date(now.time + expiryMs)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }
}
