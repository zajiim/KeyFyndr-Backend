package com.keyfyndr.backend.features.auth.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordHashService(
    private val passwordEncoder: PasswordEncoder
) {

    fun hash(rawPassword: String): String =
        passwordEncoder.encode(rawPassword)!!

    fun matches(rawPassword: String, hashedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, hashedPassword)
}
