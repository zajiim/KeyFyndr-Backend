package com.keyfyndr.backend.common.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Standard API response wrapper used across all endpoints.
 *
 * @param success Whether the request was successful
 * @param statusCode The HTTP status code
 * @param message A human-readable message describing the result
 * @param data The response payload (null for error responses or message-only responses)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(statusCode: Int = 200, message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(success = true, statusCode = statusCode, message = message, data = data)
        }

        fun <T> error(statusCode: Int, message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(success = false, statusCode = statusCode, message = message, data = data)
        }
    }
}
