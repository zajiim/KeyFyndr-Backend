package com.keyfyndr.backend.features.auth.controller

import com.keyfyndr.backend.common.response.ApiResponse
import com.keyfyndr.backend.features.auth.dto.request.*
import com.keyfyndr.backend.features.auth.dto.response.LoginResponse
import com.keyfyndr.backend.features.auth.dto.response.RegisterResponse
import com.keyfyndr.backend.features.auth.usecase.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val resendOtpUseCase: ResendOtpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sendPhoneOtpUseCase: SendPhoneOtpUseCase,
    private val verifyPhoneOtpUseCase: VerifyPhoneOtpUseCase
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<RegisterResponse>> {
        val response = registerUserUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success(statusCode = HttpStatus.CREATED.value(), message = "Registration successful. Please verify your email with the OTP sent.", data = response)
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = loginUserUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = "Login successful", data = response)
        )
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@Valid @RequestBody request: VerifyOtpRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = verifyOtpUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = "OTP verified successfully", data = response)
        )
    }

    @PostMapping("/phone/send-otp")
    fun sendPhoneOtp(@Valid @RequestBody request: PhoneLoginRequest): ResponseEntity<ApiResponse<Nothing>> {
        val message = sendPhoneOtpUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = message)
        )
    }

    @PostMapping("/phone/verify-otp")
    fun verifyPhoneOtp(@Valid @RequestBody request: VerifyPhoneOtpRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = verifyPhoneOtpUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = "Phone OTP verified successfully", data = response)
        )
    }

    @PostMapping("/resend-otp")
    fun resendOtp(@Valid @RequestBody request: ResendOtpRequest): ResponseEntity<ApiResponse<Nothing>> {
        val message = resendOtpUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = message)
        )
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = refreshTokenUseCase.execute(request)
        return ResponseEntity.ok(
            ApiResponse.success(message = "Token refreshed successfully", data = response)
        )
    }

    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<ApiResponse<Nothing>> {
        val userId = UUID.fromString(authentication.principal as String)
        logoutUseCase.execute(userId)
        return ResponseEntity.ok(
            ApiResponse.success(message = "Logged out successfully")
        )
    }
}
