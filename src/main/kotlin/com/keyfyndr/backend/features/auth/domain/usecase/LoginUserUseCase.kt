package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.presentation.request.LoginRequest
import com.keyfyndr.backend.features.auth.presentation.response.LoginResponse
import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.domain.service.JwtService
import com.keyfyndr.backend.features.auth.domain.service.PasswordHashService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun execute(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordHashService.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        if (!user.verified) {
            throw IllegalStateException("Account not verified. Please verify your OTP first.")
        }

        val userId = user.id!!

        val accessToken = jwtService.generateAccessToken(userId)
        val refreshToken = jwtService.generateRefreshToken(userId)

        val refreshTokenDomain = RefreshToken(
            userId = userId,
            token = refreshToken,
            expiryDate = Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs())
        )
        refreshTokenRepository.save(refreshTokenDomain)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId.toString(),
            email = user.email,
            phone = user.phone
        )
    }
}
