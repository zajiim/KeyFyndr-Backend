package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.presentation.request.ResendOtpRequest
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.domain.service.OtpService
import org.springframework.stereotype.Component

@Component
class ResendOtpUseCase(
    private val userRepository: UserRepository,
    private val otpService: OtpService
) {

    fun execute(request: ResendOtpRequest): String {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("No account found with this email")

        if (user.verified) {
            throw IllegalStateException("Account is already verified")
        }

        val otp = otpService.generateAndStore(request.email)
        // TODO: Send OTP via email/SMS service
        println("Resent OTP for ${request.email}: $otp") // Remove in production

        return "OTP has been resent to ${request.email}"
    }
}
