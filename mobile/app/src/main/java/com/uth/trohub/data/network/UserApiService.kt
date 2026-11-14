package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.UserResponse
import com.uth.trohub.data.model.UserUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {

    @GET("users/myInfo")
    suspend fun getMyInfo(): Response<ApiResponse<UserResponse>>

    @PUT("users/myInfo")
    suspend fun updateMyInfo(
        @Body request: UserUpdateRequest
    ): Response<ApiResponse<UserResponse>>
}
