package com.uth.trohub.data.model

data class PropertyResponse(
    val id: String,
    val name: String,
    val address: String? = null,
    val description: String? = null,
    val myRole: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PropertyRequest(
    val name: String,
    val address: String? = null,
    val description: String? = null
)

data class MemberResponse(
    val userId: String,
    val username: String,
    val fullName: String?,
    val role: String
)

data class MemberRequest(
    val username: String,
    val role: String // OWNER, MANAGER
)
