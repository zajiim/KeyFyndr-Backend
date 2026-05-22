package com.keyfyndr.backend.features.auth.usecase

import com.keyfyndr.backend.features.auth.dto.request.RefreshTokenRequest
import com.keyfyndr.backend.features.auth.dto.response.LoginResponse
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.service.JwtService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RefreshTokenUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository,
    private val jwtService: JwtService
) {

    fun execute(request: RefreshTokenRequest): LoginResponse {
        println("DEBUG: Looking up refresh token: ${request.refreshToken.take(20)}...${request.refreshToken.takeLast(20)}")
        println("DEBUG: Token length: ${request.refreshToken.length}")

        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)

        if (storedToken == null) {
            println("DEBUG: Token NOT FOUND in database")
            throw IllegalArgumentException("Invalid refresh token")
        }

        println("DEBUG: Token found! Expiry: ${storedToken.expiryDate}, Now: ${Instant.now()}")

        if (storedToken.expiryDate.isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken)
            throw IllegalArgumentException("Refresh token has expired. Please login again.")
        }

        val user = userRepository.findById(storedToken.userId)
            ?: throw IllegalArgumentException("User not found")

        val userId = user.id!!
        val newAccessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        // Rotate: save new token (old one gets replaced)
        val updatedToken = storedToken.copy(
            token = newRefreshToken,
            expiryDate = Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs())
        )
        refreshTokenRepository.save(updatedToken)

        return LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            userId = userId.toString(),
            email = user.email,
            phone = user.phone
        )
    }
}
