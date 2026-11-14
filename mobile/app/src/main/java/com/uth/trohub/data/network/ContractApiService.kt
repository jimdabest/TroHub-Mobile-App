package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.ContractCreationRequest
import com.uth.trohub.data.model.ContractResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ContractApiService {

    @GET("properties/{propertyId}/contracts")
    suspend fun list(
        @Path("propertyId") propertyId: String,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<ContractResponse>>>

    @GET("properties/{propertyId}/contracts/{contractId}")
    suspend fun get(
        @Path("propertyId") propertyId: String,
        @Path("contractId") contractId: String
    ): Response<ApiResponse<ContractResponse>>

    @POST("properties/{propertyId}/contracts")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Body request: ContractCreationRequest
    ): Response<ApiResponse<ContractResponse>>

    @POST("properties/{propertyId}/contracts/{contractId}/terminate")
    suspend fun terminate(
        @Path("propertyId") propertyId: String,
        @Path("contractId") contractId: String
    ): Response<ApiResponse<ContractResponse>>
}
