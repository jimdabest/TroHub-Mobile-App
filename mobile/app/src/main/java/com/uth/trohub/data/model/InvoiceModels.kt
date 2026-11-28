package com.uth.trohub.data.model

data class InvoiceSummaryResponse(
    val id: String,
    val propertyId: String,
    val roomId: String,
    val roomCode: String? = null,
    val tenantName: String? = null,
    val period: String,            // "2026-11-01"
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val status: String,            // UNPAID | PAID | OVERDUE
    val dueDate: String? = null,
    val createdAt: String? = null
)

data class InvoiceItemResponse(
    val id: String,
    val invoiceId: String,
    val type: String,              // ROOM_RENT | ELECTRICITY | WATER | OTHER
    val description: String,
    val quantity: Double? = null,
    val unitPrice: Double? = null,
    val amount: Double
)

data class InvoiceResponse(
    val id: String,
    val propertyId: String,
    val roomId: String,
    val roomCode: String? = null,
    val tenantName: String? = null,
    val tenantPhone: String? = null,
    val period: String,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val status: String,
    val dueDate: String? = null,
    val items: List<InvoiceItemResponse> = emptyList()
)

data class InvoiceGenerationRequest(
    val period: String,            // "2026-11-01"
    val dueDate: String? = null    // "2026-11-15"
)

data class InvoiceGenerationResponse(
    val period: String,
    val generated: Int,
    val skipped: Int,
    val invoices: List<InvoiceSummaryResponse> = emptyList()
)

data class InvoiceItemRequest(
    val description: String,
    val amount: Double
)
