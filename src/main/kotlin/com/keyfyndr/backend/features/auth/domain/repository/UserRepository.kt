package com.keyfyndr.backend.features.auth.domain.repository

import com.keyfyndr.backend.features.auth.domain.model.User

/**
 * Domain repository interface for User operations.
 * Use cases depend on this abstraction, not on the JPA implementation.
 */
interface UserRepository {

    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun findById(id: java.util.UUID): User?
}
