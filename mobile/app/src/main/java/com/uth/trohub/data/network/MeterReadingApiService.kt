package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.MeterReadingRequest
import com.uth.trohub.data.model.MeterReadingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MeterReadingApiService {

    @POST("properties/{propertyId}/meter-readings")
    suspend fun create(
        @Path("propertyId") propertyId: String,
        @Body request: MeterReadingRequest
    ): Response<ApiResponse<MeterReadingResponse>>

    @GET("properties/{propertyId}/rooms/{roomId}/meter-readings/latest")
    suspend fun latest(
        @Path("propertyId") propertyId: String,
        @Path("roomId") roomId: String
    ): Response<ApiResponse<List<MeterReadingResponse>>>

    @GET("properties/{propertyId}/meter-readings")
    suspend fun listByPeriod(
        @Path("propertyId") propertyId: String,
        @Query("period") period: String
    ): Response<ApiResponse<List<MeterReadingResponse>>>
}
