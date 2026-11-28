package com.uth.trohub.data.model

data class MonthlyRevenueSummary(
    val period: String,            // "2026-11-01"
    val totalRevenue: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalUnpaid: Double = 0.0,
    val invoiceCount: Int = 0
)

data class RevenueReportApiResponse(
    val propertyId: String,
    val from: String,
    val to: String,
    val months: List<MonthlyRevenueSummary> = emptyList(),
    val grandTotal: Double = 0.0,
    val grandPaid: Double = 0.0
)
