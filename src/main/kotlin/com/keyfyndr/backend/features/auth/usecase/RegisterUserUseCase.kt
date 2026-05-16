package com.keyfyndr.backend.features.auth.usecase

import com.keyfyndr.backend.features.auth.dto.response.RegisterResponse
import com.keyfyndr.backend.features.auth.dto.request.RegisterRequest
import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.service.OtpService
import com.keyfyndr.backend.features.auth.service.PasswordHashService
import org.springframework.stereotype.Component

@Component
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService,
    private val otpService: OtpService
) {

    fun execute(request: RegisterRequest): RegisterResponse {
        val existingUser = userRepository.findByEmail(request.email)

        if (existingUser != null) {
            if (existingUser.verified) {
                throw IllegalArgumentException("An account with this email already exists")
            } else {
                // User exists but is not verified — resend OTP
                val otp = otpService.generateAndStore(request.email)
                // TODO: Send OTP via email/SMS service
                println("Resent OTP for ${request.email}: $otp") // Remove in production
                throw IllegalStateException("Please verify your email first. A new OTP has been sent.")
            }
        }

        val hashedPassword = passwordHashService.hash(request.password)

        val user = User(
            name = request.name,
            email = request.email,
            phone = request.phone,
            passwordHash = hashedPassword,
            verified = false
        )

        userRepository.save(user)

        val otp = otpService.generateAndStore(request.email)
        // TODO: Send OTP via email/SMS service
        println("Generated OTP for ${request.email}: $otp") // Remove in production

        return RegisterResponse(
            email = request.email
        )
    }
}
