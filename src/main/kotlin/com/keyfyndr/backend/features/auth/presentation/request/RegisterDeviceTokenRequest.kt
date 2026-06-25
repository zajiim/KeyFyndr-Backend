package com.keyfyndr.backend.features.auth.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * Request body for registering an FCM device token.
 * Called by the Android client after receiving a token from Firebase.
 */
data class RegisterDeviceTokenRequest(

    @field:NotBlank(message = "Device token must not be blank")
    val deviceToken: String,

    @field:NotBlank(message = "Device type must not be blank")
    @field:Pattern(
        regexp = "ANDROID|IOS",
        flags = [Pattern.Flag.CASE_INSENSITIVE],
        message = "Device type must be 'ANDROID' or 'IOS'"
    )
    val deviceType: String = "ANDROID"
)
