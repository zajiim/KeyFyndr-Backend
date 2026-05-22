package com.keyfyndr.backend.features.auth.data.service

import com.keyfyndr.backend.features.auth.domain.service.PasswordHashService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * BCrypt-backed implementation of [PasswordHashService].
 *
 * Uses Spring Security's [PasswordEncoder] (configured as BCryptPasswordEncoder
 * in SecurityConfig) for hashing and verification.
 */
@Service
class PasswordHashServiceImpl(
    private val passwordEncoder: PasswordEncoder
) : PasswordHashService {

    override fun hash(rawPassword: String): String =
        passwordEncoder.encode(rawPassword)!!

    override fun matches(rawPassword: String, hashedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, hashedPassword)
}
