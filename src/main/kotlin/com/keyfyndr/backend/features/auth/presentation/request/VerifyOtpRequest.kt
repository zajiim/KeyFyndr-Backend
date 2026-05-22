package com.keyfyndr.backend.features.auth.presentation.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class VerifyOtpRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "OTP is required")
    val otp: String
)
