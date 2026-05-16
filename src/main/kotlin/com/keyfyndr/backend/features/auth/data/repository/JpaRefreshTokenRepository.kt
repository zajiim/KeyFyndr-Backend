package com.keyfyndr.backend.features.auth.data.repository

import com.keyfyndr.backend.features.auth.data.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {

    fun findByToken(token: String): RefreshTokenEntity?

    fun deleteByUserId(userId: UUID)
}
