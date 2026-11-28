package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.RevenueReportApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RevenueReportApiService {

    @GET("properties/{propertyId}/reports/revenue")
    suspend fun revenue(
        @Path("propertyId") propertyId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<ApiResponse<RevenueReportApiResponse>>
}
