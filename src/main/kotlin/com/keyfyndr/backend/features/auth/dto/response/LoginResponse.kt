package com.keyfyndr.backend.features.auth.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String?,
    val phone: String?
)
