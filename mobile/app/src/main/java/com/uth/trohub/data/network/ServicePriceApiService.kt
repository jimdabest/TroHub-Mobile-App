package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.ServicePriceRequest
import com.uth.trohub.data.model.ServicePriceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ServicePriceApiService {

    @GET("properties/{propertyId}/service-prices")
    suspend fun list(
        @Path("propertyId") propertyId: String
    ): Response<ApiResponse<List<ServicePriceResponse>>>

    @POST("properties/{propertyId}/service-prices")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Body request: ServicePriceRequest
    ): Response<ApiResponse<ServicePriceResponse>>

    @PUT("properties/{propertyId}/service-prices/{serviceId}")
    suspend fun update(
        @Path("propertyId") propertyId: String,
        @Path("serviceId") serviceId: String,
        @Body request: ServicePriceRequest
    ): Response<ApiResponse<ServicePriceResponse>>

    @DELETE("properties/{propertyId}/service-prices/{serviceId}")
    suspend fun delete(
        @Path("propertyId") propertyId: String,
        @Path("serviceId") serviceId: String
    ): Response<ApiResponse<Void>>
}
