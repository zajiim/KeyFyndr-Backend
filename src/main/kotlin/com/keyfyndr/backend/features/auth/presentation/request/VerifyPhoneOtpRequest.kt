package com.keyfyndr.backend.features.auth.presentation.request

import jakarta.validation.constraints.NotBlank

data class VerifyPhoneOtpRequest(
    @field:NotBlank(message = "Phone number is required")
    val phone: String,

    @field:NotBlank(message = "OTP is required")
    val otp: String
)
