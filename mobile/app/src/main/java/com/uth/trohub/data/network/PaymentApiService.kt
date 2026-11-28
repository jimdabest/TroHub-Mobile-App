package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.InvoiceSummaryResponse
import com.uth.trohub.data.model.PaymentCreationResponse
import com.uth.trohub.data.model.PaymentRequest
import com.uth.trohub.data.model.PaymentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApiService {

    @POST("properties/{propertyId}/invoices/{invoiceId}/payments")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String,
        @Body request: PaymentRequest
    ): Response<ApiResponse<PaymentCreationResponse>>

    @GET("properties/{propertyId}/invoices/{invoiceId}/payments")
    suspend fun list(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String
    ): Response<ApiResponse<List<PaymentResponse>>>

    @DELETE("properties/{propertyId}/invoices/{invoiceId}/payments/{paymentId}")
    suspend fun void(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String,
        @Path("paymentId") paymentId: String
    ): Response<ApiResponse<InvoiceSummaryResponse>>
}
