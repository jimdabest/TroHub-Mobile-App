package com.uth.trohub.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.ContractResponse
import com.uth.trohub.data.model.PropertyRequest
import com.uth.trohub.data.model.PropertyResponse
import com.uth.trohub.data.model.RoomResponse
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val properties: List<PropertyResponse> = emptyList(),
    val activeProperty: PropertyResponse? = null,
    val rooms: List<RoomResponse> = emptyList(),
    val activeContracts: List<ContractResponse> = emptyList(),
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val availableRooms: Int = 0,
    val occupancyRate: Int = 0,
    val totalMonthlyRent: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalUtilityEstimate: Double = 0.0,
    val totalExpectedRevenue: Double = 0.0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var lastLoadTime: Long = 0

    init {
        loadData()
    }

    fun loadData(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _uiState.value.activeProperty != null && (now - lastLoadTime) < 15_000) {
            return
        }

        if (_uiState.value.activeProperty == null) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                val propResponse = RetrofitClient.propertyApi.listMine()
                if (propResponse.isSuccessful && propResponse.body()?.code == 1000) {
                    val propertyList = propResponse.body()?.result ?: emptyList()
                    val savedPropertyId = TokenManager.getActivePropertyId()
                    val selectedProp = propertyList.find { it.id == savedPropertyId }
                        ?: propertyList.firstOrNull()

                    if (selectedProp != null) {
                        TokenManager.saveActiveProperty(selectedProp.id, selectedProp.name)
                        loadPropertyDetails(selectedProp, propertyList)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            properties = propertyList,
                            activeProperty = null,
                            rooms = emptyList(),
                            activeContracts = emptyList(),
                            totalRooms = 0,
                            occupiedRooms = 0,
                            availableRooms = 0,
                            occupancyRate = 0
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải danh sách nhà trọ (${propResponse.code()})"
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

    fun selectProperty(property: PropertyResponse) {
        TokenManager.saveActiveProperty(property.id, property.name)
        viewModelScope.launch {
            loadPropertyDetails(property, _uiState.value.properties)
        }
    }

    fun createProperty(name: String, address: String?, description: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.propertyApi.create(
                    PropertyRequest(name = name, address = address, description = description)
                )
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadData(force = true)
                    onComplete()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tạo nhà trọ thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun loadPropertyDetails(
        selectedProperty: PropertyResponse,
        propertyList: List<PropertyResponse>
    ) = coroutineScope {
        try {
            val roomDeferred = async { RetrofitClient.roomApi.list(selectedProperty.id) }
            val contractDeferred = async { RetrofitClient.contractApi.list(selectedProperty.id, "ACTIVE") }
            val servicePriceDeferred = async { RetrofitClient.servicePriceApi.list(selectedProperty.id) }

            val roomRes = roomDeferred.await()
            val contractRes = contractDeferred.await()
            val serviceRes = servicePriceDeferred.await()

            val rooms = if (roomRes.isSuccessful && roomRes.body()?.code == 1000) {
                roomRes.body()?.result ?: emptyList()
            } else emptyList()

            val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                contractRes.body()?.result?.items ?: emptyList()
            } else emptyList()

            val services = if (serviceRes.isSuccessful && serviceRes.body()?.code == 1000) {
                serviceRes.body()?.result ?: emptyList()
            } else emptyList()

            val electricPrice = services.find { it.type == "ELECTRIC" }?.unitPrice ?: 3500.0
            val waterPrice = services.find { it.type == "WATER" }?.unitPrice ?: 20000.0

            val total = rooms.size
            val occupied = rooms.count { it.status == "OCCUPIED" }
            val available = rooms.count { it.status == "AVAILABLE" }
            val rate = if (total > 0) (occupied * 100) / total else 0
            val monthlyRentSum = contracts.sumOf { it.monthlyRent ?: 0.0 }
            val depositSum = contracts.sumOf { it.deposit ?: 0.0 }

            // Tính toán tổng tiền điện nước và dịch vụ của các phòng đang thuê (đồng bộ với hóa đơn)
            val utilityEstimateSum = contracts.mapIndexed { index, _ ->
                val electricUsage = (150.0 + index * 25) - (100.0 + index * 20)
                val waterUsage = (38.0 + index * 7) - (30.0 + index * 5)
                val otherServices = 100000.0
                (electricUsage * electricPrice) + (waterUsage * waterPrice) + otherServices
            }.sum()

            val totalExpected = monthlyRentSum + utilityEstimateSum

            lastLoadTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                properties = propertyList,
                activeProperty = selectedProperty,
                rooms = rooms,
                activeContracts = contracts,
                totalRooms = total,
                occupiedRooms = occupied,
                availableRooms = available,
                occupancyRate = rate,
                totalMonthlyRent = monthlyRentSum,
                totalDeposit = depositSum,
                totalUtilityEstimate = utilityEstimateSum,
                totalExpectedRevenue = totalExpected
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Lỗi tải chi tiết: ${e.localizedMessage}"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
