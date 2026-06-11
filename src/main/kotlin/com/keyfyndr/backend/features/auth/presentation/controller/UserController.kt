package com.keyfyndr.backend.features.auth.presentation.controller

import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.auth.domain.usecase.GetUserProfileUseCase
import com.keyfyndr.backend.features.auth.presentation.response.UserProfileResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for authenticated User operations.
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val getUserProfileUseCase: GetUserProfileUseCase
) {

    @GetMapping("/me")
    fun getMe(authentication: Authentication): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val userId = when (val principal = authentication.principal) {
            is UUID -> principal
            is String -> UUID.fromString(principal)
            else -> throw IllegalStateException("Unexpected principal type: $principal")
        }

        val user = getUserProfileUseCase.execute(userId)

        val response = UserProfileResponse(
            id = user.id.toString(),
            name = user.name,
            email = user.email,
            phone = user.phone
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "User profile retrieved successfully",
                data = response
            )
        )
    }
}
