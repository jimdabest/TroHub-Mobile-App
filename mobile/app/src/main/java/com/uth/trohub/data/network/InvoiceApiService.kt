package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.InvoiceGenerationRequest
import com.uth.trohub.data.model.InvoiceGenerationResponse
import com.uth.trohub.data.model.InvoiceItemRequest
import com.uth.trohub.data.model.InvoiceResponse
import com.uth.trohub.data.model.InvoiceSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InvoiceApiService {

    @POST("properties/{propertyId}/invoices/generate")
    suspend fun generate(
        @Path("propertyId") propertyId: String,
        @Body request: InvoiceGenerationRequest
    ): Response<ApiResponse<InvoiceGenerationResponse>>

    @GET("properties/{propertyId}/invoices")
    suspend fun list(
        @Path("propertyId") propertyId: String,
        @Query("period") period: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<InvoiceSummaryResponse>>>

    @GET("properties/{propertyId}/invoices/{invoiceId}")
    suspend fun get(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String
    ): Response<ApiResponse<InvoiceResponse>>

    @POST("properties/{propertyId}/invoices/{invoiceId}/items")
    suspend fun addItem(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String,
        @Body request: InvoiceItemRequest
    ): Response<ApiResponse<InvoiceResponse>>

    @DELETE("properties/{propertyId}/invoices/{invoiceId}/items/{itemId}")
    suspend fun removeItem(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String,
        @Path("itemId") itemId: String
    ): Response<ApiResponse<InvoiceResponse>>

    @DELETE("properties/{propertyId}/invoices/{invoiceId}")
    suspend fun delete(
        @Path("propertyId") propertyId: String,
        @Path("invoiceId") invoiceId: String
    ): Response<ApiResponse<Unit>>
}
