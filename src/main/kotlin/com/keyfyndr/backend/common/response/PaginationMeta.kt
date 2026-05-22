package com.keyfyndr.backend.common.response

/**
 * Reusable pagination metadata for any paginated API response.
 * Lives in the common layer so any feature can use it.
 */
data class PaginationMeta(
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)
