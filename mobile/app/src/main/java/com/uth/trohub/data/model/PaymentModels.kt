package com.uth.trohub.data.model

data class PaymentRequest(
    val amount: Double,
    val method: String,            // CASH | BANK_TRANSFER
    val note: String? = null
)

data class PaymentResponse(
    val id: String,
    val invoiceId: String,
    val amount: Double,
    val method: String,
    val note: String? = null,
    val createdAt: String? = null
)

data class PaymentCreationResponse(
    val payment: PaymentResponse,
    val invoice: InvoiceSummaryResponse
)
