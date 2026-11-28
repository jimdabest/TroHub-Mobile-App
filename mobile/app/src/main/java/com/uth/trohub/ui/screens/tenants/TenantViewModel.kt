package com.uth.trohub.ui.screens.tenants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.TenantRequest
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TenantItemUiModel(
    val id: String,
    val fullName: String,
    val phone: String?,
    val idNumber: String?,
    val note: String?,
    val rentingRoomCode: String? = null
)

data class TenantListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val propertyName: String = "",
    val tenants: List<TenantItemUiModel> = emptyList(),
    val searchQuery: String = ""
)

class TenantViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TenantListUiState())
    val uiState: StateFlow<TenantListUiState> = _uiState.asStateFlow()
    private var lastLoadTime: Long = 0

    init {
        loadTenants()
    }

    fun loadTenants(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _uiState.value.tenants.isNotEmpty() && (now - lastLoadTime) < 15_000) {
            return
        }

        val propertyId = TokenManager.getActivePropertyId()
        val propertyName = TokenManager.getActivePropertyName() ?: "Khu nhà trọ"

        if (propertyId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Vui lòng chọn hoặc tạo nhà trọ ở trang chủ trước"
            )
            return
        }

        if (_uiState.value.tenants.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                propertyName = propertyName
            )
        }

        viewModelScope.launch {
            try {
                val tenantDeferred = async { RetrofitClient.tenantApi.list(propertyId) }
                val contractDeferred = async { RetrofitClient.contractApi.list(propertyId, "ACTIVE") }

                val tenantRes = tenantDeferred.await()
                val contractRes = contractDeferred.await()

                if (tenantRes.isSuccessful && tenantRes.body()?.code == 1000) {
                    val rawTenants = tenantRes.body()?.result ?: emptyList()
                    val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                        contractRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val contractByTenantId = contracts.associateBy { it.tenantId }

                    val uiTenants = rawTenants.map { tenant ->
                        val contract = contractByTenantId[tenant.id]
                        TenantItemUiModel(
                            id = tenant.id,
                            fullName = tenant.fullName,
                            phone = tenant.phone,
                            idNumber = tenant.idNumber,
                            note = tenant.note,
                            rentingRoomCode = contract?.roomCode
                        )
                    }

                    lastLoadTime = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tenants = uiTenants,
                        propertyName = propertyName
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải danh sách khách (${tenantRes.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun createTenant(
        fullName: String,
        phone: String?,
        idNumber: String?,
        note: String?,
        onSuccess: () -> Unit
    ) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val req = TenantRequest(
                    fullName = fullName,
                    phone = phone,
                    idNumber = idNumber,
                    note = note
                )
                val res = RetrofitClient.tenantApi.create(propertyId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadTenants(force = true)
                    _uiState.value = _uiState.value.copy(successMessage = "Thêm khách thuê thành công")
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Thêm thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun updateTenant(
        tenantId: String,
        fullName: String,
        phone: String?,
        idNumber: String?,
        note: String?,
        onSuccess: () -> Unit
    ) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val req = TenantRequest(
                    fullName = fullName,
                    phone = phone,
                    idNumber = idNumber,
                    note = note
                )
                val res = RetrofitClient.tenantApi.update(propertyId, tenantId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadTenants(force = true)
                    _uiState.value = _uiState.value.copy(successMessage = "Cập nhật thành công")
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Cập nhật thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun deleteTenant(tenantId: String) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.tenantApi.delete(propertyId, tenantId)
                if (res.isSuccessful) {
                    loadTenants(force = true)
                    _uiState.value = _uiState.value.copy(successMessage = "Đã xóa khách thuê")
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Không thể xóa: ${res.body()?.message ?: "Khách đang có hợp đồng hoặc lỗi server"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun inviteTenant(tenantId: String, onResult: (String?) -> Unit) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.tenantApi.invite(propertyId, tenantId)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    val code = res.body()?.result?.code
                    _uiState.value = _uiState.value.copy(successMessage = "Mã liên kết: $code")
                    onResult(code)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tạo mã mời thất bại: ${res.body()?.message ?: res.code()}"
                    )
                    onResult(null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
                onResult(null)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
