package com.keyfyndr.backend.features.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class PhoneLoginRequest(
    @field:NotBlank(message = "Phone number is required")
    val phone: String
)
