package com.uth.trohub.data.model

data class RoomResponse(
    val id: String,
    val propertyId: String,
    val code: String,
    val block: String? = null,
    val floor: Int? = null,
    val areaM2: Double? = null,
    val price: Double,
    val status: String, // AVAILABLE, OCCUPIED, MAINTENANCE
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class RoomRequest(
    val code: String,
    val block: String? = null,
    val floor: Int? = null,
    val areaM2: Double? = null,
    val price: Double,
    val description: String? = null
)

data class RoomStatusRequest(
    val status: String // AVAILABLE, MAINTENANCE
)
