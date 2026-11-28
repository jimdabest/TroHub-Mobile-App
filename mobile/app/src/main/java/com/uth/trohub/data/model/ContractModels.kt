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
    val occupants: Int? = null,
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

data class ContractRenewRequest(
    val endDate: String,
    val monthlyRent: Double? = null
)

data class ContractOccupantsRequest(
    val occupants: Int
)

data class TerminateContractRequest(
    val leaveDate: String? = null,
    val depositRefunded: Double? = null,
    val depositDeducted: Double? = null,
    val depositNote: String? = null
)

data class TenantInviteResponse(
    val code: String,
    val expiresAt: String
)
