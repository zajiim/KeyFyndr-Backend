package com.keyfyndr.backend.features.auth.domain.usecase

import com.keyfyndr.backend.common.exception.ResourceNotFoundException
import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetUserProfileUseCase(
    private val userRepository: UserRepository
) {

    fun execute(userId: UUID): User {
        return userRepository.findById(userId)
            ?: throw ResourceNotFoundException("User not found with id: $userId")
    }
}
