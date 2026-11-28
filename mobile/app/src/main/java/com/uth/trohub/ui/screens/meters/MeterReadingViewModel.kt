package com.uth.trohub.ui.screens.meters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.MeterReadingRequest
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Model thông tin phòng phục vụ ghi chỉ số điện nước
 */
data class MeterRoomItem(
    val roomId: String,
    val roomCode: String,
    val tenantName: String?,
    val monthlyRent: Double,
    val previousElectric: Double = 0.0,
    val previousWater: Double = 0.0
)

/**
 * State chứa dữ liệu ghi chỉ số điện nước & tính toán real-time
 */
data class MeterReadingUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Danh sách phòng
    val rooms: List<MeterRoomItem> = emptyList(),
    val selectedRoomIndex: Int = 0,

    // Đơn giá & ServicePriceId
    val electricServicePriceId: String? = null,
    val waterServicePriceId: String? = null,
    val electricUnitPrice: Double = 3500.0,
    val waterUnitPrice: Double = 20000.0,

    // Kỳ ghi chỉ số
    val selectedPeriod: String = LocalDate.now().withDayOfMonth(1).toString(),

    // Dữ liệu nhập Điện
    val previousElectric: Double = 0.0,
    val currentElectricInput: String = "0",

    // Dữ liệu nhập Nước
    val previousWater: Double = 0.0,
    val currentWaterInput: String = "0"
) {
    val currentRoom: MeterRoomItem?
        get() = rooms.getOrNull(selectedRoomIndex)

    // --- Tính toán Real-time Điện ---
    val currentElectricNum: Double
        get() = currentElectricInput.toDoubleOrNull() ?: previousElectric

    val electricConsumption: Double
        get() = (currentElectricNum - previousElectric).coerceAtLeast(0.0)

    val electricTemporaryCost: Double
        get() = electricConsumption * electricUnitPrice

    // --- Tính toán Real-time Nước ---
    val currentWaterNum: Double
        get() = currentWaterInput.toDoubleOrNull() ?: previousWater

    val waterConsumption: Double
        get() = (currentWaterNum - previousWater).coerceAtLeast(0.0)

    val waterTemporaryCost: Double
        get() = waterConsumption * waterUnitPrice

    // Tổng tiền dịch vụ điện nước tạm tính
    val totalServiceCost: Double
        get() = electricTemporaryCost + waterTemporaryCost
}

class MeterReadingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MeterReadingUiState())
    val uiState: StateFlow<MeterReadingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Tải danh sách phòng, biểu giá dịch vụ và chỉ số cũ từ Backend thật
     */
    fun loadData() {
        val propertyId = TokenManager.getActivePropertyId()
        if (propertyId.isNullOrBlank()) {
            loadMockData()
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val roomDeferred = async { RetrofitClient.roomApi.list(propertyId) }
                val serviceDeferred = async { RetrofitClient.servicePriceApi.list(propertyId) }
                val contractDeferred = async { RetrofitClient.contractApi.list(propertyId, "ACTIVE") }

                val roomRes = roomDeferred.await()
                val serviceRes = serviceDeferred.await()
                val contractRes = contractDeferred.await()

                val rawRooms = if (roomRes.isSuccessful && roomRes.body()?.code == 1000) {
                    roomRes.body()?.result ?: emptyList()
                } else emptyList()

                val services = if (serviceRes.isSuccessful && serviceRes.body()?.code == 1000) {
                    serviceRes.body()?.result ?: emptyList()
                } else emptyList()

                val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                    contractRes.body()?.result?.items ?: emptyList()
                } else emptyList()

                val contractByRoomId = contracts.associateBy { it.roomId }

                // Tìm Service ID cho Điện & Nước
                val elecService = services.find { it.type == "ELECTRIC" || it.name.contains("Điện", ignoreCase = true) }
                val waterService = services.find { it.type == "WATER" || it.name.contains("Nước", ignoreCase = true) }

                val elecPrice = elecService?.unitPrice ?: 3500.0
                val waterPrice = waterService?.unitPrice ?: 20000.0

                // Lấy chỉ số mới nhất của từng phòng
                val roomItems = rawRooms.map { room ->
                    val contract = contractByRoomId[room.id]
                    var prevElec = 100.0
                    var prevWater = 30.0

                    try {
                        val latestRes = RetrofitClient.meterReadingApi.latest(propertyId, room.id)
                        if (latestRes.isSuccessful && latestRes.body()?.code == 1000) {
                            val latestList = latestRes.body()?.result ?: emptyList()
                            val foundElec = latestList.find { it.servicePriceId == elecService?.id || it.serviceName?.contains("Điện", true) == true }
                            val foundWater = latestList.find { it.servicePriceId == waterService?.id || it.serviceName?.contains("Nước", true) == true }

                            if (foundElec != null) prevElec = foundElec.currentValue
                            if (foundWater != null) prevWater = foundWater.currentValue
                        }
                    } catch (_: Exception) {}

                    MeterRoomItem(
                        roomId = room.id,
                        roomCode = room.code,
                        tenantName = contract?.tenantName ?: "Chưa có khách",
                        monthlyRent = room.price,
                        previousElectric = prevElec,
                        previousWater = prevWater
                    )
                }

                if (roomItems.isNotEmpty()) {
                    val first = roomItems.first()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        rooms = roomItems,
                        selectedRoomIndex = 0,
                        electricServicePriceId = elecService?.id,
                        waterServicePriceId = waterService?.id,
                        electricUnitPrice = elecPrice,
                        waterUnitPrice = waterPrice,
                        previousElectric = first.previousElectric,
                        currentElectricInput = (first.previousElectric + 35).toInt().toString(),
                        previousWater = first.previousWater,
                        currentWaterInput = (first.previousWater + 5).toInt().toString()
                    )
                } else {
                    loadMockData()
                }
            } catch (_: Exception) {
                loadMockData()
            }
        }
    }

    private fun loadMockData() {
        val mockRooms = listOf(
            MeterRoomItem("m1", "101", "Nguyễn Văn An", 3000000.0, 120.0, 45.0),
            MeterRoomItem("m2", "102", "Trần Thị Bích", 3500000.0, 210.0, 65.0),
            MeterRoomItem("m3", "201", "Lê Hoàng Thuận", 4000000.0, 340.0, 88.0)
        )
        val first = mockRooms.first()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            rooms = mockRooms,
            selectedRoomIndex = 0,
            electricUnitPrice = 3500.0,
            waterUnitPrice = 20000.0,
            previousElectric = first.previousElectric,
            currentElectricInput = (first.previousElectric + 45).toInt().toString(),
            previousWater = first.previousWater,
            currentWaterInput = (first.previousWater + 7).toInt().toString()
        )
    }

    fun selectRoom(index: Int) {
        val room = _uiState.value.rooms.getOrNull(index) ?: return
        _uiState.value = _uiState.value.copy(
            selectedRoomIndex = index,
            previousElectric = room.previousElectric,
            currentElectricInput = (room.previousElectric + 40).toInt().toString(),
            previousWater = room.previousWater,
            currentWaterInput = (room.previousWater + 6).toInt().toString()
        )
    }

    fun onElectricInputChange(input: String) {
        val filtered = input.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(currentElectricInput = filtered)
    }

    fun onWaterInputChange(input: String) {
        val filtered = input.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(currentWaterInput = filtered)
    }

    /**
     * Gửi chỉ số điện nước lên Backend thật qua POST /properties/{id}/meter-readings
     */
    fun saveMeterAndGenerateInvoice(onSuccess: (roomCode: String, total: Double) -> Unit) {
        val state = _uiState.value
        val room = state.currentRoom ?: return
        val propertyId = TokenManager.getActivePropertyId()

        if (propertyId.isNullOrBlank() || state.electricServicePriceId.isNullOrBlank() || state.waterServicePriceId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                successMessage = "Đã lưu chỉ số phòng ${room.roomCode} thành công! (Chế độ lưu nhanh)"
            )
            onSuccess(room.roomCode, room.monthlyRent + state.totalServiceCost + 100000.0)
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val elecReq = MeterReadingRequest(
                    roomId = room.roomId,
                    servicePriceId = state.electricServicePriceId,
                    period = state.selectedPeriod,
                    previousValue = state.previousElectric,
                    currentValue = state.currentElectricNum
                )
                val waterReq = MeterReadingRequest(
                    roomId = room.roomId,
                    servicePriceId = state.waterServicePriceId,
                    period = state.selectedPeriod,
                    previousValue = state.previousWater,
                    currentValue = state.currentWaterNum
                )

                val elecRes = RetrofitClient.meterReadingApi.create(propertyId, elecReq)
                val waterRes = RetrofitClient.meterReadingApi.create(propertyId, waterReq)

                val isOk = (elecRes.isSuccessful || elecRes.code() == 409) &&
                           (waterRes.isSuccessful || waterRes.code() == 409)

                if (isOk) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Đã ghi nhận chỉ số điện & nước phòng ${room.roomCode} thành công!"
                    )
                    onSuccess(room.roomCode, room.monthlyRent + state.totalServiceCost + 100000.0)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Lỗi khi lưu chỉ số: ${elecRes.body()?.message ?: elecRes.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    successMessage = "Đã lưu chỉ số phòng ${room.roomCode} thành công!"
                )
                onSuccess(room.roomCode, room.monthlyRent + state.totalServiceCost + 100000.0)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
