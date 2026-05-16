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
        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

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
