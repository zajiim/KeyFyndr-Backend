package com.keyfyndr.backend.features.auth.presentation.request

import jakarta.validation.constraints.NotBlank

/**
 * Request body for removing an FCM device token.
 * Called by the Android client on logout or when a new token is issued.
 */
data class UnregisterDeviceTokenRequest(

    @field:NotBlank(message = "Device token must not be blank")
    val deviceToken: String
)
