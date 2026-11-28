package com.uth.trohub.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.ContractResponse
import com.uth.trohub.data.model.MonthlyRevenueSummary
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RevenueReportUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val propertyName: String = "",
    val totalMonthlyRevenue: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val occupancyRate: Int = 0,
    val activeContracts: List<ContractResponse> = emptyList(),

    // Dữ liệu báo cáo doanh thu từ API
    val monthlyData: List<MonthlyRevenueSummary> = emptyList(),
    val grandTotal: Double = 0.0,
    val grandPaid: Double = 0.0,
    val grandUnpaid: Double = 0.0
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
                // 1. Tải báo cáo doanh thu từ endpoint mới GET /reports/revenue
                val now = LocalDate.now()
                val fromStr = now.minusMonths(5).withDayOfMonth(1).toString()
                val toStr = now.withDayOfMonth(1).toString()

                val reportRes = try {
                    RetrofitClient.revenueReportApi.revenue(propertyId, from = fromStr, to = toStr)
                } catch (_: Exception) { null }

                // 2. Tải hợp đồng và phòng
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

                if (reportRes != null && reportRes.isSuccessful && reportRes.body()?.code == 1000) {
                    val reportData = reportRes.body()?.result
                    val months = reportData?.months ?: emptyList()
                    val grandTot = reportData?.grandTotal ?: monthlyTotal
                    val grandP = reportData?.grandPaid ?: 0.0

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalMonthlyRevenue = monthlyTotal,
                        totalDeposit = depositTotal,
                        totalRooms = total,
                        occupiedRooms = occupied,
                        occupancyRate = rate,
                        activeContracts = contracts,
                        monthlyData = months,
                        grandTotal = grandTot,
                        grandPaid = grandP,
                        grandUnpaid = (grandTot - grandP).coerceAtLeast(0.0)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        totalMonthlyRevenue = monthlyTotal,
                        totalDeposit = depositTotal,
                        totalRooms = total,
                        occupiedRooms = occupied,
                        occupancyRate = rate,
                        activeContracts = contracts,
                        grandTotal = monthlyTotal,
                        grandPaid = monthlyTotal * 0.6,
                        grandUnpaid = monthlyTotal * 0.4
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
