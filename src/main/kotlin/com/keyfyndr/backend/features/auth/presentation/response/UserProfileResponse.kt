package com.keyfyndr.backend.features.auth.presentation.response

/**
 * Response returned for user profile details (/api/v1/users/me).
 */
data class UserProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val phone: String
)
