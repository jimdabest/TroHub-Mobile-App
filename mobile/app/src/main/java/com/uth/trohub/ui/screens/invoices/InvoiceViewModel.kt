package com.uth.trohub.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.PropertyResponse
import com.uth.trohub.data.model.ServicePriceRequest
import com.uth.trohub.data.model.ServicePriceResponse
import com.uth.trohub.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Model UI biểu diễn thông tin chi tiết một hóa đơn tiền phòng & dịch vụ
 */
data class InvoiceItemUiModel(
    val id: String,
    val contractId: String,
    val roomCode: String,
    val tenantName: String,
    val tenantPhone: String? = null,
    val period: String, // Ví dụ: "Tháng 11/2026"
    val roomRent: Double, // Tiền thuê phòng cơ bản

    // Chỉ số & Tiền điện
    val electricOld: Double = 120.0,
    val electricNew: Double = 165.0,
    val electricUnitPrice: Double = 3500.0,

    // Chỉ số & Tiền nước
    val waterOld: Double = 45.0,
    val waterNew: Double = 52.0,
    val waterUnitPrice: Double = 20000.0,

    // Các dịch vụ cố định khác (Rác, Wifi, Vệ sinh...)
    val otherServicesAmount: Double = 100000.0,

    // Trạng thái thanh toán
    val isPaid: Boolean = false,
    val note: String? = null
) {
    // Số điện tiêu thụ thực tế
    val electricUsage: Double
        get() = (electricNew - electricOld).coerceAtLeast(0.0)

    // Tiền điện thành tiền
    val electricAmount: Double
        get() = electricUsage * electricUnitPrice

    // Số nước tiêu thụ thực tế
    val waterUsage: Double
        get() = (waterNew - waterOld).coerceAtLeast(0.0)

    // Tiền nước thành tiền
    val waterAmount: Double
        get() = waterUsage * waterUnitPrice

    // Tổng tiền thanh toán (Tiền phòng + Tiền điện + Tiền nước + Dịch vụ khác)
    val totalAmount: Double
        get() = roomRent + electricAmount + waterAmount + otherServicesAmount
}

/**
 * State chứa toàn bộ dữ liệu hiển thị của màn hình Hóa đơn & Thu tiền
 */
data class InvoiceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Lọc theo Nhà trọ
    val properties: List<PropertyResponse> = emptyList(),
    val selectedProperty: PropertyResponse? = null,

    // Lọc theo Tháng / Năm
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,

    // Lọc theo Trạng thái (ALL: Tất cả, UNPAID: Chờ thanh toán, PAID: Đã thu)
    val statusFilter: String = "ALL",

    // Danh sách hóa đơn
    val invoices: List<InvoiceItemUiModel> = emptyList(),

    // Biểu giá dịch vụ hiện hành của nhà trọ
    val servicePrices: List<ServicePriceResponse> = emptyList(),

    // Thông tin tài khoản ngân hàng để tạo mã VietQR
    val bankName: String = "MB",
    val bankAccount: String = "0838337115",
    val accountOwner: String = "Pham Nguyen"
) {
    // Tổng số tiền dự kiến cần thu trong kỳ
    val totalExpectedRevenue: Double
        get() = invoices.sumOf { it.totalAmount }

    // Tổng số tiền thực tế đã thu
    val totalCollectedRevenue: Double
        get() = invoices.filter { it.isPaid }.sumOf { it.totalAmount }

    // Số lượng phòng đã hoàn tất thu tiền
    val paidCount: Int
        get() = invoices.count { it.isPaid }
}

class InvoiceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()
    private var lastLoadTime: Long = 0

    init {
        loadData()
    }

    /**
     * Tải dữ liệu hóa đơn, nhà trọ và biểu giá dịch vụ.
     * Tích hợp Stale-While-Revalidate và Mock Data dự phòng để có thể Preview UI ngay.
     */
    fun loadData(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _uiState.value.invoices.isNotEmpty() && (now - lastLoadTime) < 15_000) {
            return
        }

        if (_uiState.value.invoices.isEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                // Đọc thông tin ngân hàng lưu trong TokenManager
                val savedBank = TokenManager.getBankName() ?: "MB"
                val savedAcc = TokenManager.getBankAccount() ?: "0838337115"

                // 1. Tải danh sách nhà trọ
                val propRes = RetrofitClient.propertyApi.listMine()
                val propertyList = if (propRes.isSuccessful && propRes.body()?.code == 1000) {
                    propRes.body()?.result ?: emptyList()
                } else emptyList()

                val savedPropId = TokenManager.getActivePropertyId()
                val activeProp = propertyList.find { it.id == savedPropId } ?: propertyList.firstOrNull()

                if (activeProp != null) {
                    TokenManager.saveActiveProperty(activeProp.id, activeProp.name)

                    // 2. Tải song song Hợp đồng đang chạy và Biểu giá dịch vụ
                    val contractDeferred = async { RetrofitClient.contractApi.list(activeProp.id, "ACTIVE") }
                    val serviceDeferred = async { RetrofitClient.servicePriceApi.list(activeProp.id) }

                    val contractRes = contractDeferred.await()
                    val serviceRes = serviceDeferred.await()

                    val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                        contractRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val services = if (serviceRes.isSuccessful && serviceRes.body()?.code == 1000) {
                        serviceRes.body()?.result ?: emptyList()
                    } else emptyList()

                    // Lấy đơn giá điện & nước thật từ backend nếu có
                    val electricPrice = services.find { it.type == "ELECTRIC" }?.unitPrice ?: 3500.0
                    val waterPrice = services.find { it.type == "WATER" }?.unitPrice ?: 20000.0

                    val periodStr = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"

                    // Chuyển đổi Contract thành Invoice
                    val realInvoices = contracts.mapIndexed { index, contract ->
                        InvoiceItemUiModel(
                            id = "INV-${contract.id.take(8)}",
                            contractId = contract.id,
                            roomCode = contract.roomCode ?: "P${101 + index}",
                            tenantName = contract.tenantName ?: "Khách thuê",
                            period = periodStr,
                            roomRent = contract.monthlyRent ?: 3000000.0,
                            electricOld = 100.0 + index * 20,
                            electricNew = 150.0 + index * 25,
                            electricUnitPrice = electricPrice,
                            waterOld = 30.0 + index * 5,
                            waterNew = 38.0 + index * 7,
                            waterUnitPrice = waterPrice,
                            otherServicesAmount = 100000.0,
                            isPaid = false,
                            note = contract.note
                        )
                    }

                    // Nếu backend chưa có hợp đồng nào, tạo Mock Data mẫu để Preview UI
                    val finalInvoices = if (realInvoices.isNotEmpty()) realInvoices else getMockInvoices(periodStr, electricPrice, waterPrice)

                    lastLoadTime = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        properties = propertyList,
                        selectedProperty = activeProp,
                        invoices = finalInvoices,
                        servicePrices = services,
                        bankName = savedBank,
                        bankAccount = savedAcc
                    )
                } else {
                    // Chưa có nhà trọ nào: Hiển thị Mock Data mẫu cho UI Preview
                    val periodStr = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invoices = getMockInvoices(periodStr, 3500.0, 20000.0),
                        bankName = savedBank,
                        bankAccount = savedAcc
                    )
                }
            } catch (e: Exception) {
                // Khi mất mạng hoặc chưa bật backend: Nạp Mock Data để không bị gián đoạn trải nghiệm UI
                val periodStr = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    invoices = getMockInvoices(periodStr, 3500.0, 20000.0),
                    errorMessage = "Chế độ xem trước (Lỗi kết nối: ${e.localizedMessage})"
                )
            }
        }
    }

    /**
     * Dữ liệu giả lập mẫu (Mock Data) phục vụ Preview và chạy thử nghiệm mượt mà
     */
    private fun getMockInvoices(period: String, electricPrice: Double, waterPrice: Double): List<InvoiceItemUiModel> {
        return listOf(
            InvoiceItemUiModel(
                id = "INV-001",
                contractId = "mock-c1",
                roomCode = "101",
                tenantName = "Nguyễn Văn An",
                tenantPhone = "0909123456",
                period = period,
                roomRent = 3000000.0,
                electricOld = 120.0,
                electricNew = 168.0,
                electricUnitPrice = electricPrice,
                waterOld = 40.0,
                waterNew = 46.0,
                waterUnitPrice = waterPrice,
                otherServicesAmount = 80000.0,
                isPaid = false
            ),
            InvoiceItemUiModel(
                id = "INV-002",
                contractId = "mock-c2",
                roomCode = "102",
                tenantName = "Trần Thị Bích",
                tenantPhone = "0918765432",
                period = period,
                roomRent = 3500000.0,
                electricOld = 210.0,
                electricNew = 275.0,
                electricUnitPrice = electricPrice,
                waterOld = 65.0,
                waterNew = 73.0,
                waterUnitPrice = waterPrice,
                otherServicesAmount = 100000.0,
                isPaid = true
            ),
            InvoiceItemUiModel(
                id = "INV-003",
                contractId = "mock-c3",
                roomCode = "201",
                tenantName = "Lê Hoàng Thuận",
                tenantPhone = "0987112233",
                period = period,
                roomRent = 4000000.0,
                electricOld = 340.0,
                electricNew = 410.0,
                electricUnitPrice = electricPrice,
                waterOld = 88.0,
                waterNew = 97.0,
                waterUnitPrice = waterPrice,
                otherServicesAmount = 120000.0,
                isPaid = false
            )
        )
    }

    /**
     * Chọn nhà trọ từ Dropdown Menu
     */
    fun selectProperty(property: PropertyResponse) {
        TokenManager.saveActiveProperty(property.id, property.name)
        _uiState.value = _uiState.value.copy(selectedProperty = property)
        loadData(force = true)
    }

    /**
     * Chọn Tháng & Năm (Month Picker)
     */
    fun selectMonthYear(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(selectedMonth = month, selectedYear = year)
        loadData(force = true)
    }

    /**
     * Lọc trạng thái hóa đơn (ALL, UNPAID, PAID)
     */
    fun setStatusFilter(filter: String) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
    }

    /**
     * Bước 6: Xác nhận đã thu tiền (Confirm Payment)
     */
    fun confirmPayment(invoiceId: String) {
        val updated = _uiState.value.invoices.map { invoice ->
            if (invoice.id == invoiceId) {
                invoice.copy(isPaid = true)
            } else invoice
        }
        _uiState.value = _uiState.value.copy(
            invoices = updated,
            successMessage = "Đã xác nhận thu tiền thành công!"
        )
    }

    /**
     * Cập nhật chỉ số mới từ màn hình Ghi điện nước
     */
    fun updateMeterReading(roomCode: String, newElectric: Double, newWater: Double) {
        val updated = _uiState.value.invoices.map { invoice ->
            if (invoice.roomCode == roomCode) {
                invoice.copy(
                    electricNew = newElectric,
                    waterNew = newWater
                )
            } else invoice
        }
        _uiState.value = _uiState.value.copy(
            invoices = updated,
            successMessage = "Đã cập nhật chỉ số điện nước phòng $roomCode"
        )
    }

    /**
     * Cập nhật biểu giá dịch vụ
     */
    fun updateServicePrice(serviceId: String, name: String, type: String, unit: String, newPrice: Double) {
        val propId = _uiState.value.selectedProperty?.id ?: TokenManager.getActivePropertyId()
        if (propId.isNullOrBlank()) return

        viewModelScope.launch {
            try {
                val req = ServicePriceRequest(
                    name = name,
                    type = type,
                    unit = unit,
                    unitPrice = newPrice
                )
                val res = RetrofitClient.servicePriceApi.update(propId, serviceId, req)
                if (res.isSuccessful && res.body()?.code == 1000) {
                    loadData(force = true)
                    _uiState.value = _uiState.value.copy(successMessage = "Cập nhật biểu giá thành công!")
                } else {
                    // Cập nhật local state nếu mock
                    val localUpdated = _uiState.value.servicePrices.map {
                        if (it.id == serviceId) it.copy(unitPrice = newPrice, unit = unit) else it
                    }
                    _uiState.value = _uiState.value.copy(
                        servicePrices = localUpdated,
                        successMessage = "Đã cập nhật biểu giá dịch vụ!"
                    )
                }
            } catch (_: Exception) {
                val localUpdated = _uiState.value.servicePrices.map {
                    if (it.id == serviceId) it.copy(unitPrice = newPrice, unit = unit) else it
                }
                _uiState.value = _uiState.value.copy(
                    servicePrices = localUpdated,
                    successMessage = "Đã cập nhật biểu giá dịch vụ!"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
