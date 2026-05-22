package com.keyfyndr.backend.common.response

/**
 * Framework-agnostic page result used at the domain layer.
 *
 * This avoids leaking Spring's Page<T> into use cases and domain code.
 * The data layer maps Spring's Page to this, and the presentation layer
 * maps this to PaginationMeta for the API response.
 */
data class PageResult<T>(
    val content: List<T>,
    val currentPage: Int,
    val pageSize: Int,
    val totalItems: Long,
    val totalPages: Int
) {
    val hasNextPage: Boolean get() = currentPage < totalPages
    val hasPreviousPage: Boolean get() = currentPage > 1

    fun toPaginationMeta(): PaginationMeta = PaginationMeta(
        currentPage = currentPage,
        pageSize = pageSize,
        totalItems = totalItems,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
        hasPreviousPage = hasPreviousPage
    )
}
