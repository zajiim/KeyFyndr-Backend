package com.keyfyndr.backend.features.auth.presentation.response

/**
 * Response returned after successful user registration.
 * Contains the registered email for confirmation display.
 */
data class RegisterResponse(
    val email: String
)
