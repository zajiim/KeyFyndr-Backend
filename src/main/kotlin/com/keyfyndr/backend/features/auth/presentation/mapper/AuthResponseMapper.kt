package com.keyfyndr.backend.features.auth.presentation.mapper

import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.presentation.response.LoginResponse
import com.keyfyndr.backend.features.auth.presentation.response.RegisterResponse

/**
 * Presentation-layer mappers: Domain model → Response DTOs.
 *
 * These are separate from the data-layer mappers (entity ↔ domain)
 * to maintain strict layer boundaries:
 *   Entity ↔ Domain (data layer)
 *   Domain → Response (presentation layer)
 */

/** Maps a domain [User] to a [RegisterResponse] after successful registration. */
fun User.toRegisterResponse(): RegisterResponse = RegisterResponse(
    email = this.email
)

/**
 * Creates a [LoginResponse] from user and token details.
 *
 * Utility for building the login response used across multiple use cases
 * (login, OTP verification, token refresh, phone OTP verification).
 */
fun buildLoginResponse(
    accessToken: String,
    refreshToken: String,
    userId: String,
    email: String?,
    phone: String?
): LoginResponse = LoginResponse(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    email = email,
    phone = phone
)
