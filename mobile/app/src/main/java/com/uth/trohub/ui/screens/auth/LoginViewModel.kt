package com.uth.trohub.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.AuthRequest
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val token: String? = null,
    val currentServerUrl: String = TokenManager.getBaseUrl()
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateServerUrl(newUrl: String) {
        TokenManager.saveBaseUrl(newUrl)
        _uiState.value = _uiState.value.copy(currentServerUrl = TokenManager.getBaseUrl(), errorMessage = null)
    }

    fun login(username: String, pass: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = AuthRequest(username.trim(), pass)
                val response = RetrofitClient.authApi.login(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.code == 1000) {
                        val token = apiResponse.result?.token
                        if (token != null) {
                            TokenManager.saveToken(token)
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            token = token
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = apiResponse?.message ?: "Đăng nhập thất bại (Mã: ${apiResponse?.code})"
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    var msg = "Tên đăng nhập hoặc mật khẩu không chính xác"
                    if (!errorBody.isNullOrBlank()) {
                        try {
                            val json = JSONObject(errorBody)
                            val code = json.optInt("code", 0)
                            val serverMsg = json.optString("message", "")
                            msg = if (code == 1003 || serverMsg.contains("Chưa đăng nhập")) {
                                "Tên đăng nhập hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!"
                            } else if (serverMsg.isNotBlank()) {
                                serverMsg
                            } else {
                                "Tên đăng nhập hoặc mật khẩu không chính xác"
                            }
                        } catch (_: Exception) {}
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = msg
                    )
                }
            } catch (e: Exception) {
                val server = TokenManager.getBaseUrl()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể kết nối đến máy chủ ($server): ${e.localizedMessage ?: "Vui lòng kiểm tra lại IP hoặc bật backend."}"
                )
            }
        }
    }

    fun testConnection(targetUrl: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanUrl = if (targetUrl.endsWith("/")) targetUrl else "$targetUrl/"
                val healthUrl = cleanUrl + "actuator/health"
                val request = Request.Builder().url(healthUrl).build()
                val testClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build()
                val response = testClient.newCall(request).execute()
                if (response.isSuccessful) {
                    onResult(true, "Kết nối thành công (HTTP ${response.code})")
                } else {
                    onResult(false, "Máy chủ phản hồi mã lỗi: HTTP ${response.code}")
                }
            } catch (e: Exception) {
                onResult(false, "Không thể kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
