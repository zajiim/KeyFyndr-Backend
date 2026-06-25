package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.model.DeviceToken
import com.keyfyndr.backend.features.auth.domain.repository.DeviceTokenRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Registers (or refreshes) an FCM device token for the authenticated user.
 *
 * Logic:
 * - If the token string already exists (belongs to this or another user),
 *   it is deleted first then re-saved for the current user.
 *   This handles the scenario where the OS reassigns a token to a new account
 *   on the same device (e.g., user logs out, another logs in).
 * - If the token is new, it is simply persisted.
 */
@Component
class RegisterDeviceTokenUseCase(
    private val deviceTokenRepository: DeviceTokenRepository
) {

    fun execute(userId: UUID, token: String, deviceType: String) {
        // Remove stale ownership if another user previously held this token
        if (deviceTokenRepository.existsByDeviceToken(token)) {
            deviceTokenRepository.deleteByToken(token)
        }

        deviceTokenRepository.save(
            DeviceToken(
                userId = userId,
                deviceToken = token,
                deviceType = deviceType.uppercase()
            )
        )
    }
}
