package com.keyfyndr.backend.features.auth.presentation.response

/**
 * Response returned after successful login, OTP verification, or token refresh.
 * Contains JWT tokens and user identification details.
 */
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String?,
    val phone: String?
)
