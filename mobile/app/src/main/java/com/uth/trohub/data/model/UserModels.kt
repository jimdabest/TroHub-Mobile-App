package com.uth.trohub.data.model

data class UserResponse(
    val id: String,
    val username: String,
    val fullName: String?,
    val phone: String?,
    val email: String?,
    val role: String?
)

data class UserUpdateRequest(
    val fullName: String,
    val phone: String? = null,
    val email: String? = null
)
