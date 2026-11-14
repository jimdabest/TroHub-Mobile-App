package com.uth.trohub.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.LogoutRequest
import com.uth.trohub.data.model.ServicePriceRequest
import com.uth.trohub.data.model.ServicePriceResponse
import com.uth.trohub.data.model.UserResponse
import com.uth.trohub.data.model.UserUpdateRequest
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userProfile: UserResponse? = null,
    val servicePrices: List<ServicePriceResponse> = emptyList(),
    val bankName: String = "",
    val bankAccount: String = "",
    val isLoggedOut: Boolean = false
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private var lastLoadTime: Long = 0

    init {
        loadData()
    }

    fun loadData(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _uiState.value.userProfile != null && (now - lastLoadTime) < 15_000) {
            return
        }

        if (_uiState.value.userProfile == null) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        }

        viewModelScope.launch {
            try {
                val propertyId = TokenManager.getActivePropertyId()

                val userDeferred = async { RetrofitClient.userApi.getMyInfo() }
                val serviceDeferred = async {
                    if (!propertyId.isNullOrBlank()) {
                        RetrofitClient.servicePriceApi.list(propertyId)
                    } else null
                }

                val userRes = userDeferred.await()
                val sRes = serviceDeferred.await()

                val profile = if (userRes.isSuccessful && userRes.body()?.code == 1000) {
                    userRes.body()?.result
                } else null

                val services = if (sRes != null && sRes.isSuccessful && sRes.body()?.code == 1000) {
                    sRes.body()?.result ?: emptyList()
                } else emptyList()

                lastLoadTime = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = profile,
                    servicePrices = services,
                    bankName = TokenManager.getBankName() ?: "",
                    bankAccount = TokenManager.getBankAccount() ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi tải thông tin cài đặt: ${e.localizedMessage}"
                )
            }
        }
    }

    fun updateProfile(fullName: String, phone: String?, email: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch {
            try {
                val req = UserUpdateRequest(
                    fullName = fullName.trim(),
                    phone = phone?.trim()?.ifEmpty { null },
                    email = email?.trim()?.ifEmpty { null }
                )
                val res = RetrofitClient.userApi.updateMyInfo(req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = res.body()?.result,
                        successMessage = "Cập nhật thông tin thành công"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Cập nhật thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi: ${e.localizedMessage}"
                )
            }
        }
    }

    fun addServicePrice(name: String, type: String, unit: String?, unitPrice: Double) {
        val propertyId = TokenManager.getActivePropertyId()
        if (propertyId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng chọn nhà trọ trước khi thêm giá dịch vụ")
            return
        }

        val resolvedUnit = if (!unit.isNullOrBlank()) {
            unit.trim()
        } else {
            when (type) {
                "ELECTRIC" -> "kWh"
                "WATER" -> "m³"
                "PER_PERSON" -> "người"
                else -> "tháng"
            }
        }

        viewModelScope.launch {
            try {
                val req = ServicePriceRequest(
                    name = name.trim(),
                    type = type,
                    unit = resolvedUnit,
                    unitPrice = unitPrice
                )
                val res = RetrofitClient.servicePriceApi.create(propertyId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadData(force = true)
                    _uiState.value = _uiState.value.copy(successMessage = "Thêm giá dịch vụ thành công")
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Thêm dịch vụ thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun saveBankInfo(bankName: String, accountNumber: String) {
        TokenManager.saveBankInfo(bankName.trim(), accountNumber.trim())
        _uiState.value = _uiState.value.copy(
            bankName = bankName.trim(),
            bankAccount = accountNumber.trim(),
            successMessage = "Đã lưu thông tin tài khoản ngân hàng"
        )
    }

    fun logout() {
        val token = TokenManager.getToken()
        viewModelScope.launch {
            if (!token.isNullOrBlank()) {
                try {
                    RetrofitClient.authApi.logout(LogoutRequest(token))
                } catch (_: Exception) {
                    // Tiếp tục xóa session kể cả khi mạng lỗi
                }
            }
            TokenManager.clearSession()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
