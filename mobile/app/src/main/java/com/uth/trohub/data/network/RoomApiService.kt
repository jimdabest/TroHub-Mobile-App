package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.RoomRequest
import com.uth.trohub.data.model.RoomResponse
import com.uth.trohub.data.model.RoomStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApiService {

    @GET("properties/{propertyId}/rooms")
    suspend fun list(
        @Path("propertyId") propertyId: String,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<RoomResponse>>>

    @GET("properties/{propertyId}/rooms/{roomId}")
    suspend fun get(
        @Path("propertyId") propertyId: String,
        @Path("roomId") roomId: String
    ): Response<ApiResponse<RoomResponse>>

    @POST("properties/{propertyId}/rooms")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Body request: RoomRequest
    ): Response<ApiResponse<RoomResponse>>

    @PUT("properties/{propertyId}/rooms/{roomId}")
    suspend fun update(
        @Path("propertyId") propertyId: String,
        @Path("roomId") roomId: String,
        @Body request: RoomRequest
    ): Response<ApiResponse<RoomResponse>>

    @PATCH("properties/{propertyId}/rooms/{roomId}/status")
    suspend fun changeStatus(
        @Path("propertyId") propertyId: String,
        @Path("roomId") roomId: String,
        @Body request: RoomStatusRequest
    ): Response<ApiResponse<RoomResponse>>

    @DELETE("properties/{propertyId}/rooms/{roomId}")
    suspend fun delete(
        @Path("propertyId") propertyId: String,
        @Path("roomId") roomId: String
    ): Response<ApiResponse<Unit>>
}
