package com.keyfyndr.backend.features.key.presentation.controller

import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.key.domain.usecase.*
import com.keyfyndr.backend.features.key.presentation.mapper.toPublicResponse
import com.keyfyndr.backend.features.key.presentation.mapper.toResponse
import com.keyfyndr.backend.features.key.presentation.mapper.toStatusResponse
import com.keyfyndr.backend.features.key.presentation.request.CreateKeyRequest
import com.keyfyndr.backend.features.key.presentation.request.MarkKeyLocationRequest
import com.keyfyndr.backend.features.key.presentation.response.KeyResponse
import com.keyfyndr.backend.features.key.presentation.response.KeyStatusResponse
import com.keyfyndr.backend.features.key.presentation.response.PublicKeyResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for the Key feature.
 *
 * Architecture decisions:
 * - No business logic in the controller — all operations delegate to use cases
 * - JWT principal is extracted as UUID from the Authentication object
 * - Public lookup endpoint requires no authentication (configured in SecurityConfig)
 * - All responses wrapped in ApiResponse for consistency with the auth feature
 */
@RestController
@RequestMapping("/api/v1/keys")
class KeyController(
    private val createKeyUseCase: CreateKeyUseCase,
    private val getMyKeysUseCase: GetMyKeysUseCase,
    private val getKeyByPublicIdUseCase: GetKeyByPublicIdUseCase,
    private val markKeyAsLostUseCase: MarkKeyAsLostUseCase,
    private val markKeyAsFoundUseCase: MarkKeyAsFoundUseCase,
    private val deleteKeyUseCase: DeleteKeyUseCase
) {

    /**
     * POST /api/v1/keys
     * POST /api/v1/keys/create-key
     * Create a new key for the authenticated user.
     */
    @PostMapping("", "/create-key")
    fun createKey(
        @Valid @RequestBody request: CreateKeyRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<KeyResponse>> {
        val ownerId = extractUserId(authentication)
        val key = createKeyUseCase.execute(request, ownerId)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(
                statusCode = HttpStatus.CREATED.value(),
                message = "Key created successfully",
                data = key.toResponse()
            )
        )
    }

    /**
     * GET /api/v1/keys/me?page=1&size=10
     * Get paginated active keys owned by the authenticated user.
     * Defaults: page=1, size=10. Results sorted by createdAt DESC.
     */
    @GetMapping("/me")
    fun getMyKeys(
        authentication: Authentication,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<List<KeyResponse>>> {
        val ownerId = extractUserId(authentication)
        val pageResult = getMyKeysUseCase.execute(ownerId, page, size)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Keys retrieved successfully",
                data = pageResult.content.map { it.toResponse() },
                pagination = pageResult.toPaginationMeta()
            )
        )
    }

    /**
     * GET /api/v1/keys/public/{publicKeyId}
     * Public endpoint — no JWT required.
     * Returns public-safe key details for crowd-based sighting.
     */
    @GetMapping("/public/{publicKeyId}")
    fun getKeyByPublicId(
        @PathVariable publicKeyId: String
    ): ResponseEntity<ApiResponse<PublicKeyResponse>> {
        val key = getKeyByPublicIdUseCase.execute(publicKeyId)

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Key found",
                data = key.toPublicResponse()
            )
        )
    }

    /**
     * PATCH /api/v1/keys/{id}/lost
     * Mark a key as lost. Only the owner can do this.
     * Optionally accepts latitude/longitude for nearby-key tracking.
     */
    @PatchMapping("/{id}/lost")
    fun markKeyAsLost(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: MarkKeyLocationRequest?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<KeyStatusResponse>> {
        val ownerId = extractUserId(authentication)
        val key = markKeyAsLostUseCase.execute(
            keyId = id,
            ownerId = ownerId,
            latitude = request?.latitude,
            longitude = request?.longitude
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Key marked as lost",
                data = key.toStatusResponse("Key has been marked as LOST")
            )
        )
    }

    /**
     * PATCH /api/v1/keys/{id}/found
     * Mark a key as found. Only the owner can do this.
     * Optionally accepts latitude/longitude for nearby-key tracking.
     */
    @PatchMapping("/{id}/found")
    fun markKeyAsFound(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: MarkKeyLocationRequest?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<KeyStatusResponse>> {
        val ownerId = extractUserId(authentication)
        val key = markKeyAsFoundUseCase.execute(
            keyId = id,
            ownerId = ownerId,
            latitude = request?.latitude,
            longitude = request?.longitude
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                message = "Key marked as found",
                data = key.toStatusResponse("Key has been marked as FOUND")
            )
        )
    }

    /**
     * DELETE /api/v1/keys/{id}
     * Soft-delete a key. Only the owner can do this.
     */
    @DeleteMapping("/{id}")
    fun deleteKey(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        val ownerId = extractUserId(authentication)
        deleteKeyUseCase.execute(id, ownerId)

        return ResponseEntity.ok(
            ApiResponse.success(message = "Key deleted successfully")
        )
    }

    /**
     * Extracts the authenticated user's UUID from the JWT principal.
     * The JwtAuthenticationFilter stores the principal as a UUID object.
     */
    private fun extractUserId(authentication: Authentication): UUID =
        when (val principal = authentication.principal) {
            is UUID -> principal
            is String -> UUID.fromString(principal)
            else -> throw IllegalStateException("Unexpected principal type: $principal")
        }
}
