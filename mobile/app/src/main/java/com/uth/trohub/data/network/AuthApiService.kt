package com.uth.trohub.data.network

import com.uth.trohub.data.model.ApiResponse
import com.uth.trohub.data.model.AuthRequest
import com.uth.trohub.data.model.AuthenticationResponse
import com.uth.trohub.data.model.LogoutRequest
import com.uth.trohub.data.model.RefreshRequest
import com.uth.trohub.data.model.RegisterRequest
import com.uth.trohub.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/token")
    suspend fun login(@Body request: AuthRequest): Response<ApiResponse<AuthenticationResponse>>

    @POST("users")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserResponse>>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<ApiResponse<Void>>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<ApiResponse<AuthenticationResponse>>
}
