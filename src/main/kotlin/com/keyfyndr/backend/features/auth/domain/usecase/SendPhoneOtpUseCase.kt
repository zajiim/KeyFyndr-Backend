package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import com.keyfyndr.backend.features.auth.presentation.request.PhoneLoginRequest
import com.keyfyndr.backend.features.auth.domain.service.OtpService
import org.springframework.stereotype.Component

@Component
class SendPhoneOtpUseCase(
    private val userRepository: UserRepository,
    private val otpService: OtpService
) {

    fun execute(request: PhoneLoginRequest): String {
        // We don't check for user existence here to support seamless signup/login
        val otp = otpService.generateAndStore(request.phone)
        
        // TODO: Integrate with actual SMS service
        println("Generated OTP for phone ${request.phone}: $otp")

        return "Verify the OTP"
    }
}
