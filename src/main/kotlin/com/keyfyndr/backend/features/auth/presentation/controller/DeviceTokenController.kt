package com.keyfyndr.backend.features.auth.presentation.controller

import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.auth.domain.usecase.RegisterDeviceTokenUseCase
import com.keyfyndr.backend.features.auth.domain.usecase.UnregisterDeviceTokenUseCase
import com.keyfyndr.backend.features.auth.presentation.request.RegisterDeviceTokenRequest
import com.keyfyndr.backend.features.auth.presentation.request.UnregisterDeviceTokenRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for FCM device token management.
 *
 * Endpoints:
 * - POST   /api/v1/users/device-token  → Register or refresh a device token
 * - DELETE /api/v1/users/device-token  → Remove a device token (on logout / token refresh)
 *
 * These endpoints are authenticated; the userId is resolved from the JWT principal.
 *
 * Android integration:
 * 1. Call POST after login or when FirebaseMessagingService.onNewToken() fires.
 * 2. Call DELETE (or rely on LogoutUseCase which clears all tokens) on logout.
 */
@RestController
@RequestMapping("/api/v1/users")
class DeviceTokenController(
    private val registerDeviceTokenUseCase: RegisterDeviceTokenUseCase,
    private val unregisterDeviceTokenUseCase: UnregisterDeviceTokenUseCase
) {

    /**
     * POST /api/v1/users/device-token
     *
     * Registers or refreshes the FCM device token for the current user.
     * If the token is already stored under a different user (e.g., device reassignment),
     * the old record is removed before saving the new one.
     */
    @PostMapping("/device-token")
    fun registerToken(
        @Valid @RequestBody request: RegisterDeviceTokenRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        val userId = extractUserId(authentication)
        registerDeviceTokenUseCase.execute(
            userId = userId,
            token = request.deviceToken,
            deviceType = request.deviceType
        )
        return ResponseEntity.ok(
            ApiResponse.success(message = "Device token registered successfully")
        )
    }

    /**
     * DELETE /api/v1/users/device-token
     *
     * Removes a specific FCM device token. Use this when the client receives
     * a new token via onNewToken() to deregister the old one.
     * On full logout, prefer calling POST /auth/logout which clears all tokens.
     */
    @DeleteMapping("/device-token")
    fun unregisterToken(
        @Valid @RequestBody request: UnregisterDeviceTokenRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        unregisterDeviceTokenUseCase.execute(token = request.deviceToken)
        return ResponseEntity.ok(
            ApiResponse.success(message = "Device token removed successfully")
        )
    }

    private fun extractUserId(authentication: Authentication): UUID =
        when (val principal = authentication.principal) {
            is UUID -> principal
            is String -> UUID.fromString(principal)
            else -> throw IllegalStateException("Unexpected principal type: $principal")
        }
}
