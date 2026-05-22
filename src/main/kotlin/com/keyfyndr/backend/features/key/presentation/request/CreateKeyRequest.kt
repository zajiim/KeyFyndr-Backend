package com.keyfyndr.backend.features.key.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request DTO for creating a new key.
 *
 * The publicKeyId is auto-generated server-side — it is NOT part of the request.
 * Owner is derived from the JWT token.
 */
data class CreateKeyRequest(

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be at most 255 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must be at most 1000 characters")
    val description: String? = null,

    @field:Size(max = 50, message = "Color must be at most 50 characters")
    val color: String? = null,

    @field:NotBlank(message = "Category is required")
    @field:Size(max = 100, message = "Category must be at most 100 characters")
    val category: String,

    val imageUrl: String? = null
)
