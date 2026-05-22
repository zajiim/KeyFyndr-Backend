package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.features.auth.domain.repository.RefreshTokenRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun execute(userId: UUID) {
        refreshTokenRepository.deleteByUserId(userId)
    }
}
