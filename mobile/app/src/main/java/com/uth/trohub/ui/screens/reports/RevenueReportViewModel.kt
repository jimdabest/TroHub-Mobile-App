package com.uth.trohub.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.ContractResponse
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RevenueReportUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val propertyName: String = "",
    val totalMonthlyRevenue: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val occupancyRate: Int = 0,
    val activeContracts: List<ContractResponse> = emptyList()
)

class RevenueReportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueReportUiState())
    val uiState: StateFlow<RevenueReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val propertyId = TokenManager.getActivePropertyId()
        val propertyName = TokenManager.getActivePropertyName() ?: "Nhà trọ"

        if (propertyId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Vui lòng chọn hoặc tạo nhà trọ trước"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            propertyName = propertyName
        )

        viewModelScope.launch {
            try {
                val contractRes = RetrofitClient.contractApi.list(propertyId, "ACTIVE")
                val roomRes = RetrofitClient.roomApi.list(propertyId)

                val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                    contractRes.body()?.result ?: emptyList()
                } else emptyList()

                val rooms = if (roomRes.isSuccessful && roomRes.body()?.code == 1000) {
                    roomRes.body()?.result ?: emptyList()
                } else emptyList()

                val total = rooms.size
                val occupied = rooms.count { it.status == "OCCUPIED" }
                val rate = if (total > 0) (occupied * 100) / total else 0
                val monthlyTotal = contracts.sumOf { it.monthlyRent ?: 0.0 }
                val depositTotal = contracts.sumOf { it.deposit ?: 0.0 }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalMonthlyRevenue = monthlyTotal,
                    totalDeposit = depositTotal,
                    totalRooms = total,
                    occupiedRooms = occupied,
                    occupancyRate = rate,
                    activeContracts = contracts
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
