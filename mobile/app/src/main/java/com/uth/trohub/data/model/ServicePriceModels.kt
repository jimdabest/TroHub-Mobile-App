package com.uth.trohub.data.model

data class ServicePriceResponse(
    val id: String,
    val propertyId: String,
    val name: String,
    val type: String, // ELECTRIC, WATER, FIXED, PER_PERSON
    val unit: String? = null,
    val unitPrice: Double,
    val active: Boolean
)

data class ServicePriceRequest(
    val name: String,
    val type: String, // ELECTRIC, WATER, FIXED, PER_PERSON
    val unit: String,
    val unitPrice: Double,
    val active: Boolean = true
)
