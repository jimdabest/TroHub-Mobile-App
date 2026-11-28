package com.uth.trohub.data.network

import com.uth.trohub.data.local.TokenManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Bộ ghi log (Interceptor): Giúp in toàn bộ dữ liệu gửi đi và nhận về ra tab Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor tự động thêm Bearer token vào header nếu người dùng đã đăng nhập
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = TokenManager.getToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    // Interceptor định tuyến động theo Base URL được lưu và tự động thử fallback nếu host chính bị lỗi
    private val hostSelectionInterceptor = Interceptor { chain ->
        var request = chain.request()
        val configuredBase = TokenManager.getBaseUrl()
        val parsedTarget = configuredBase.toHttpUrlOrNull()

        if (parsedTarget != null) {
            val newUrl = request.url.newBuilder()
                .scheme(parsedTarget.scheme)
                .host(parsedTarget.host)
                .port(parsedTarget.port)
                .build()
            request = request.newBuilder().url(newUrl).build()
        }

        try {
            chain.proceed(request)
        } catch (e: IOException) {
            val currentHost = request.url.host
            val fallbackCandidates = listOf("192.168.2.168", "10.0.2.2", "127.0.0.1")
                .filter { it != currentHost }

            var lastError: IOException = e
            for (candidateHost in fallbackCandidates) {
                try {
                    val fallbackUrl = request.url.newBuilder()
                        .host(candidateHost)
                        .build()
                    val fallbackRequest = request.newBuilder()
                        .url(fallbackUrl)
                        .build()
                    val response = chain.proceed(fallbackRequest)
                    if (response.isSuccessful || response.code in 200..499) {
                        val updatedBase = "${request.url.scheme}://$candidateHost:${request.url.port}/api/v1/"
                        TokenManager.saveBaseUrl(updatedBase)
                        return@Interceptor response
                    }
                } catch (retryEx: IOException) {
                    lastError = retryEx
                }
            }
            throw lastError
        }
    }

    // Gắn các interceptor vào OkHttpClient
    private val client = OkHttpClient.Builder()
        .addInterceptor(hostSelectionInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Khởi tạo Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(TokenManager.DEFAULT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Kết nối các Service API
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val propertyApi: PropertyApiService by lazy {
        retrofit.create(PropertyApiService::class.java)
    }

    val roomApi: RoomApiService by lazy {
        retrofit.create(RoomApiService::class.java)
    }

    val tenantApi: TenantApiService by lazy {
        retrofit.create(TenantApiService::class.java)
    }

    val contractApi: ContractApiService by lazy {
        retrofit.create(ContractApiService::class.java)
    }

    val servicePriceApi: ServicePriceApiService by lazy {
        retrofit.create(ServicePriceApiService::class.java)
    }

    val invoiceApi: InvoiceApiService by lazy {
        retrofit.create(InvoiceApiService::class.java)
    }

    val meterReadingApi: MeterReadingApiService by lazy {
        retrofit.create(MeterReadingApiService::class.java)
    }

    val paymentApi: PaymentApiService by lazy {
        retrofit.create(PaymentApiService::class.java)
    }

    val revenueReportApi: RevenueReportApiService by lazy {
        retrofit.create(RevenueReportApiService::class.java)
    }
}
