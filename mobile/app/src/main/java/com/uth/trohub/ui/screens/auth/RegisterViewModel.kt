package com.uth.trohub.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.model.RegisterRequest
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

// 1. Data class lưu trữ trạng thái màn hình Đăng ký
data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    // 2. StateFlow quản lý trạng thái, Compose sẽ "lắng nghe"
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // 3. Hàm xử lý đăng ký
    fun register(
        username: String,
        password: String,
        fullName: String,
        phone: String,
        email: String
    ) {
        // Bật trạng thái loading, xóa lỗi cũ trước khi gọi API
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val cleanUsername = username.trim()
                val cleanPassword = password.trim()
                val cleanFullName = fullName.trim()

                if (cleanUsername.length < 4) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tên đăng nhập phải từ 4 đến 50 ký tự"
                    )
                    return@launch
                }

                if (cleanPassword.length < 8) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Mật khẩu phải từ 8 đến 72 ký tự"
                    )
                    return@launch
                }

                val request = RegisterRequest(
                    username = cleanUsername,
                    password = cleanPassword,
                    fullName = cleanFullName,
                    phone = phone.trim().ifEmpty { null },
                    email = email.trim().ifEmpty { null }
                )
                val response = RetrofitClient.authApi.register(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.code == 1000) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            registerSuccess = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = apiResponse?.message ?: "Đăng ký thất bại (Mã: ${apiResponse?.code})"
                        )
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    var message = "Lỗi máy chủ: HTTP ${response.code()}"

                    if (errorBodyString != null) {
                        try {
                            val json = JSONObject(errorBodyString)
                            if (json.has("message") && !json.isNull("message")) {
                                message = json.getString("message")
                            }
                        } catch (_: Exception) {}
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }

            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể kết nối máy chủ: ${e.localizedMessage ?: "Vui lòng kiểm tra lại đường truyền."}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Reset lại màn hình đăng ký khi muốn tạo tài khoản mới sau khi đã thành công
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
