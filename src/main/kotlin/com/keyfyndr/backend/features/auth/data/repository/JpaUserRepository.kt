package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, UUID> {

    fun findByEmail(email: String): UserEntity?
    fun findByPhone(phone: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}
