package com.uth.trohub.data.model

data class ApiResponse<T>(
    val code: Int = 1000,
    val message: String? = null,
    val result: T? = null
)

data class PageResponse<T>(
    val items: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 100,
    val totalItems: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false
)

data class AuthRequest(
    val username: String,
    val password: String
)

data class AuthenticationResponse(
    val token: String,
    val authenticated: Boolean = true
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val phone: String? = null,
    val email: String? = null
)

data class LogoutRequest(
    val token: String
)

data class IntrospectRequest(
    val token: String
)

data class RefreshRequest(
    val token: String
)

