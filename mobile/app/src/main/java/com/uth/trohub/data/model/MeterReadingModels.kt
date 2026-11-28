package com.uth.trohub.data.model

data class MeterReadingRequest(
    val roomId: String,
    val servicePriceId: String,    // ID của đơn giá Điện hoặc Nước
    val period: String,            // "2026-11-01"
    val previousValue: Double,
    val currentValue: Double
)

data class MeterReadingResponse(
    val id: String,
    val propertyId: String,
    val roomId: String,
    val roomCode: String? = null,
    val servicePriceId: String,
    val serviceName: String? = null,   // "Điện" | "Nước"
    val period: String,
    val previousValue: Double,
    val currentValue: Double,
    val consumption: Double = 0.0,
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0,
    val createdAt: String? = null
)
