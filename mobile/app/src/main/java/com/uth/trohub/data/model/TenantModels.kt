package com.uth.trohub.data.model

data class TenantResponse(
    val id: String,
    val propertyId: String,
    val fullName: String,
    val phone: String? = null,
    val idNumber: String? = null,
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TenantRequest(
    val fullName: String,
    val phone: String? = null,
    val idNumber: String? = null,
    val note: String? = null
)
