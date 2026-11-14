package com.uth.trohub.ui.screens.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.ContractCreationRequest
import com.uth.trohub.data.model.RoomRequest
import com.uth.trohub.data.model.RoomStatusRequest
import com.uth.trohub.data.model.TenantRequest
import com.uth.trohub.data.model.TenantResponse
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RoomItemUiModel(
    val id: String,
    val code: String,
    val block: String?,
    val floor: Int?,
    val areaM2: Double?,
    val price: Double,
    val status: String, // AVAILABLE, OCCUPIED, MAINTENANCE
    val description: String?,
    val tenantName: String? = null,
    val tenantPhone: String? = null,
    val contractId: String? = null
)

data class RoomListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val propertyName: String = "",
    val rooms: List<RoomItemUiModel> = emptyList(),
    val tenants: List<TenantResponse> = emptyList(),
    val selectedFilter: String = "ALL", // ALL, OCCUPIED, AVAILABLE, MAINTENANCE
    val searchQuery: String = "",
    val togglingRoomId: String? = null
)

class RoomViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoomListUiState())
    val uiState: StateFlow<RoomListUiState> = _uiState.asStateFlow()
    private var lastLoadTime: Long = 0

    init {
        loadRooms()
        loadTenants()
    }

    fun loadRooms(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _uiState.value.rooms.isNotEmpty() && (now - lastLoadTime) < 15_000) {
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

        if (_uiState.value.rooms.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                propertyName = propertyName
            )
        }

        viewModelScope.launch {
            try {
                val roomDeferred = async { RetrofitClient.roomApi.list(propertyId) }
                val contractDeferred = async { RetrofitClient.contractApi.list(propertyId, "ACTIVE") }

                val roomRes = roomDeferred.await()
                val contractRes = contractDeferred.await()

                if (roomRes.isSuccessful && roomRes.body()?.code == 1000) {
                    val rawRooms = roomRes.body()?.result ?: emptyList()
                    val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                        contractRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val contractByRoomId = contracts.associateBy { it.roomId }

                    val uiRooms = rawRooms.map { room ->
                        val contract = contractByRoomId[room.id]
                        RoomItemUiModel(
                            id = room.id,
                            code = room.code,
                            block = room.block,
                            floor = room.floor,
                            areaM2 = room.areaM2,
                            price = room.price,
                            status = room.status,
                            description = room.description,
                            tenantName = contract?.tenantName,
                            contractId = contract?.id
                        )
                    }

                    lastLoadTime = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        rooms = uiRooms,
                        propertyName = propertyName
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Không thể tải danh sách phòng (${roomRes.code()})"
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

    fun loadTenants() {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.tenantApi.list(propertyId)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    _uiState.value = _uiState.value.copy(
                        tenants = res.body()?.result ?: emptyList()
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun createRoom(
        code: String,
        price: Double,
        block: String?,
        floor: Int?,
        areaM2: Double?,
        desc: String?,
        onSuccess: () -> Unit
    ) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val req = RoomRequest(
                    code = code,
                    price = price,
                    block = block,
                    floor = floor,
                    areaM2 = areaM2,
                    description = desc
                )
                val res = RetrofitClient.roomApi.create(propertyId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadRooms(force = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Thêm phòng thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun toggleRoomStatus(roomId: String, currentStatus: String) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        val newStatus = if (currentStatus == "AVAILABLE") "MAINTENANCE" else "AVAILABLE"
        val previousRooms = _uiState.value.rooms

        // Cập nhật lạc quan ngay lập tức trên UI để người dùng thấy phản hồi tức thì
        val updatedRooms = previousRooms.map { room ->
            if (room.id == roomId) room.copy(status = newStatus) else room
        }
        _uiState.value = _uiState.value.copy(
            rooms = updatedRooms,
            togglingRoomId = roomId
        )

        viewModelScope.launch {
            try {
                val res = RetrofitClient.roomApi.changeStatus(
                    propertyId,
                    roomId,
                    RoomStatusRequest(status = newStatus)
                )
                if (res.isSuccessful && res.body()?.code == 1000) {
                    _uiState.value = _uiState.value.copy(togglingRoomId = null)
                    loadRooms(force = true)
                } else {
                    // Hoàn tác nếu lỗi
                    _uiState.value = _uiState.value.copy(
                        rooms = previousRooms,
                        togglingRoomId = null,
                        errorMessage = "Không thể đổi trạng thái phòng (${res.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    rooms = previousRooms,
                    togglingRoomId = null,
                    errorMessage = "Lỗi: ${e.localizedMessage}"
                )
            }
        }
    }

    fun createContract(
        roomId: String,
        tenantId: String,
        startDate: String,
        deposit: Double?,
        monthlyRent: Double?,
        note: String?,
        onSuccess: () -> Unit
    ) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val req = ContractCreationRequest(
                    roomId = roomId,
                    tenantId = tenantId,
                    startDate = startDate,
                    deposit = deposit,
                    monthlyRent = monthlyRent,
                    note = note
                )
                val res = RetrofitClient.contractApi.create(propertyId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadRooms(force = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tạo hợp đồng thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun createTenantAndContract(
        roomId: String,
        fullName: String,
        phone: String?,
        idNumber: String?,
        startDate: String,
        deposit: Double?,
        monthlyRent: Double?,
        onSuccess: () -> Unit
    ) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val tenantReq = TenantRequest(
                    fullName = fullName,
                    phone = phone,
                    idNumber = idNumber
                )
                val tenantRes = RetrofitClient.tenantApi.create(propertyId, tenantReq)
                if (!tenantRes.isSuccessful || tenantRes.body()?.code != 1000) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Thêm khách thuê thất bại: ${tenantRes.body()?.message ?: tenantRes.code()}"
                    )
                    return@launch
                }
                val newTenantId = tenantRes.body()?.result?.id ?: return@launch
                loadTenants()

                val contractReq = ContractCreationRequest(
                    roomId = roomId,
                    tenantId = newTenantId,
                    startDate = startDate,
                    deposit = deposit,
                    monthlyRent = monthlyRent
                )
                val contractRes = RetrofitClient.contractApi.create(propertyId, contractReq)
                if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                    loadRooms(force = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Tạo hợp đồng thất bại: ${contractRes.body()?.message ?: contractRes.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun terminateContract(contractId: String, onSuccess: () -> Unit) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.contractApi.terminate(propertyId, contractId)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadRooms(force = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Thanh lý hợp đồng thất bại: ${res.body()?.message ?: res.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun deleteRoom(roomId: String) {
        val propertyId = TokenManager.getActivePropertyId() ?: return
        viewModelScope.launch {
            try {
                val res = RetrofitClient.roomApi.delete(propertyId, roomId)
                if (res.isSuccessful) {
                    loadRooms(force = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Không thể xóa phòng: Phòng đang có hợp đồng hoặc lỗi server"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
