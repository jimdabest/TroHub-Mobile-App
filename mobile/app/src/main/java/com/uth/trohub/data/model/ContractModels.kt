package com.uth.trohub.data.model

data class ContractResponse(
    val id: String,
    val propertyId: String,
    val roomId: String,
    val roomCode: String? = null,
    val tenantId: String,
    val tenantName: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val monthlyRent: Double? = null,
    val deposit: Double? = null,
    val status: String, // ACTIVE, ENDED
    val note: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ContractCreationRequest(
    val roomId: String,
    val tenantId: String,
    val startDate: String,
    val endDate: String? = null,
    val monthlyRent: Double? = null,
    val deposit: Double? = null,
    val note: String? = null
)
