package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.repository.DeviceTokenRepository
import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val deviceTokenRepository: DeviceTokenRepository
) {

    fun execute(userId: UUID) {
        refreshTokenRepository.deleteByUserId(userId)
        // Remove all FCM device tokens so this device stops receiving push notifications
        deviceTokenRepository.deleteByUserId(userId)
    }
}

