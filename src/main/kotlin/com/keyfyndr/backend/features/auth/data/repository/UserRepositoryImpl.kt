package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.mapper.toDomain
import com.keyfyndr.backend.features.auth.data.mapper.toEntity
import com.keyfyndr.backend.features.auth.domain.model.User
import com.keyfyndr.backend.features.auth.domain.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Data layer implementation of [UserRepository].
 * Bridges the domain layer to the JPA persistence layer using mappers.
 */
@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun findByEmail(email: String): User? =
        jpaUserRepository.findByEmail(email)?.toDomain()

    override fun findByPhone(phone: String): User? =
        jpaUserRepository.findByPhone(phone)?.toDomain()

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaUserRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun existsByEmail(email: String): Boolean =
        jpaUserRepository.existsByEmail(email)

    override fun findById(id: UUID): User? =
        jpaUserRepository.findById(id).orElse(null)?.toDomain()
}
