package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.model.RefreshToken
import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.presentation.request.VerifyPhoneOtpRequest
import com.keyfyndr.backend.features.auth.presentation.response.LoginResponse
import com.keyfyndr.backend.features.auth.domain.service.JwtService
import com.keyfyndr.backend.features.auth.domain.service.OtpService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class VerifyPhoneOtpUseCase(
    private val otpService: OtpService,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun execute(request: VerifyPhoneOtpRequest): LoginResponse {
        if (!otpService.verify(request.phone, request.otp)) {
            throw IllegalArgumentException("Invalid or expired OTP")
        }

        var user = userRepository.findByPhone(request.phone)

        if (user == null) {
            // Automatic registration for new phone numbers
            val newUser = User(
                name = "User ${request.phone}",
                email = "${request.phone}@phone.user", // Placeholder unique email
                phone = request.phone,
                passwordHash = "", // No password for phone-only accounts
                verified = true
            )
            user = userRepository.save(newUser)
        } else if (!user.verified) {
            // Mark existing user as verified if they logged in via phone OTP
            val verifiedUser = user.copy(verified = true)
            user = userRepository.save(verifiedUser)
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
            email = if (user.email.endsWith("@phone.user")) null else user.email,
            phone = user.phone
        )
    }
}
