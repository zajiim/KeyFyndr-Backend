package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.repository.DeviceTokenRepository
import org.springframework.stereotype.Component

/**
 * Removes a specific FCM device token from the database.
 * Called when the user logs out from a specific device, or when
 * the client receives a new token and needs to deregister the old one.
 */
@Component
class UnregisterDeviceTokenUseCase(
    private val deviceTokenRepository: DeviceTokenRepository
) {

    fun execute(token: String) {
        deviceTokenRepository.deleteByToken(token)
    }
}
