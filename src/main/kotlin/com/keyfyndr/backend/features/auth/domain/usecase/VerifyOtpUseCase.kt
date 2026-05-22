package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.presentation.request.VerifyOtpRequest
import com.keyfyndr.backend.features.auth.presentation.response.LoginResponse
import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.domain.service.JwtService
import com.keyfyndr.backend.features.auth.domain.service.OtpService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class VerifyOtpUseCase(
    private val otpService: OtpService,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun execute(request: VerifyOtpRequest): LoginResponse {
        if (!otpService.verify(request.email, request.otp)) {
            throw IllegalArgumentException("Invalid or expired OTP")
        }

        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        // Mark user as verified and persist
        val verifiedUser = user.copy(verified = true)
        userRepository.save(verifiedUser)

        val userId = verifiedUser.id!!
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
            email = verifiedUser.email,
            phone = verifiedUser.phone
        )
    }
}
