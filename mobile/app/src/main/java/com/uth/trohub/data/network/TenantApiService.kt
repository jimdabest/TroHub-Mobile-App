package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.PageResponse
import com.uth.trohub.data.model.TenantInviteResponse
import com.uth.trohub.data.model.TenantRequest
import com.uth.trohub.data.model.TenantResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TenantApiService {

    @GET("properties/{propertyId}/tenants")
    suspend fun list(
        @Path("propertyId") propertyId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<ApiResponse<PageResponse<TenantResponse>>>

    @GET("properties/{propertyId}/tenants/{tenantId}")
    suspend fun get(
        @Path("propertyId") propertyId: String,
        @Path("tenantId") tenantId: String
    ): Response<ApiResponse<TenantResponse>>

    @POST("properties/{propertyId}/tenants")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Body request: TenantRequest
    ): Response<ApiResponse<TenantResponse>>

    @PUT("properties/{propertyId}/tenants/{tenantId}")
    suspend fun update(
        @Path("propertyId") propertyId: String,
        @Path("tenantId") tenantId: String,
        @Body request: TenantRequest
    ): Response<ApiResponse<TenantResponse>>

    @DELETE("properties/{propertyId}/tenants/{tenantId}")
    suspend fun delete(
        @Path("propertyId") propertyId: String,
        @Path("tenantId") tenantId: String
    ): Response<ApiResponse<Unit>>

    @POST("properties/{propertyId}/tenants/{tenantId}/invite")
    suspend fun invite(
        @Path("propertyId") propertyId: String,
        @Path("tenantId") tenantId: String
    ): Response<ApiResponse<TenantInviteResponse>>
}
