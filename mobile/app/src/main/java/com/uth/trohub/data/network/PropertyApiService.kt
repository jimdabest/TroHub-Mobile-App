package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.MemberRequest
import com.uth.trohub.data.model.MemberResponse
import com.uth.trohub.data.model.PropertyRequest
import com.uth.trohub.data.model.PropertyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PropertyApiService {

    @GET("properties")
    suspend fun listMine(): Response<ApiResponse<List<PropertyResponse>>>

    @GET("properties/{propertyId}")
    suspend fun get(
        @Path("propertyId") propertyId: String
    ): Response<ApiResponse<PropertyResponse>>

    @POST("properties")
    suspend fun create(
        @Body request: PropertyRequest
    ): Response<ApiResponse<PropertyResponse>>

    @PUT("properties/{propertyId}")
    suspend fun update(
        @Path("propertyId") propertyId: String,
        @Body request: PropertyRequest
    ): Response<ApiResponse<PropertyResponse>>

    @DELETE("properties/{propertyId}")
    suspend fun delete(
        @Path("propertyId") propertyId: String
    ): Response<ApiResponse<Unit>>

    @GET("properties/{propertyId}/members")
    suspend fun listMembers(
        @Path("propertyId") propertyId: String
    ): Response<ApiResponse<List<MemberResponse>>>

    @POST("properties/{propertyId}/members")
    suspend fun addMember(
        @Path("propertyId") propertyId: String,
        @Body request: MemberRequest
    ): Response<ApiResponse<MemberResponse>>

    @DELETE("properties/{propertyId}/members/{userId}")
    suspend fun removeMember(
        @Path("propertyId") propertyId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Unit>>
}
