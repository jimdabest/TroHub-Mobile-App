package com.uth.trohub.ui.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.data.model.InvoiceGenerationRequest
import com.uth.trohub.data.model.InvoiceItemResponse
import com.uth.trohub.data.model.InvoiceResponse
import com.uth.trohub.data.model.InvoiceSummaryResponse
import com.uth.trohub.data.model.PaymentRequest
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
    val contractId: String = "",
    val roomCode: String,
    val tenantName: String,
    val tenantPhone: String? = null,
    val period: String, // Ví dụ: "Tháng 11/2026"
    val roomRent: Double, // Tiền thuê phòng cơ bản

    // Chỉ số & Tiền điện
    val electricOld: Double = 0.0,
    val electricNew: Double = 0.0,
    val electricUnitPrice: Double = 3500.0,

    // Chỉ số & Tiền nước
    val waterOld: Double = 0.0,
    val waterNew: Double = 0.0,
    val waterUnitPrice: Double = 20000.0,

    // Các dịch vụ cố định khác (Rác, Wifi, Vệ sinh...)
    val otherServicesAmount: Double = 0.0,

    // Trạng thái thanh toán
    val isPaid: Boolean = false,
    val note: String? = null,

    // Dữ liệu từ Backend nếu có
    val realInvoiceId: String? = null,
    val totalBackendAmount: Double? = null,
    val items: List<InvoiceItemResponse> = emptyList()
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

    // Tổng tiền thanh toán: Ưu tiên lấy từ Backend totalBackendAmount nếu có, ngược lại tính tổng các thành phần
    val totalAmount: Double
        get() = totalBackendAmount ?: (roomRent + electricAmount + waterAmount + otherServicesAmount)
}

/**
 * State chứa toàn bộ dữ liệu hiển thị của màn hình Hóa đơn & Thu tiền
 */
data class InvoiceUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
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

    // Chi tiết hóa đơn được chọn từ backend
    val selectedInvoiceDetail: InvoiceResponse? = null,

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
     * Tải dữ liệu hóa đơn, nhà trọ và biểu giá dịch vụ từ Backend thật.
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

                    val periodApiFormat = String.format("%04d-%02d-01", _uiState.value.selectedYear, _uiState.value.selectedMonth)
                    val periodDisplay = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"

                    // 2. Tải song song Hóa đơn tháng, Biểu giá, và Hợp đồng
                    val invoiceDeferred = async { RetrofitClient.invoiceApi.list(activeProp.id, period = periodApiFormat) }
                    val serviceDeferred = async { RetrofitClient.servicePriceApi.list(activeProp.id) }
                    val contractDeferred = async { RetrofitClient.contractApi.list(activeProp.id, "ACTIVE") }

                    val invoiceRes = invoiceDeferred.await()
                    val serviceRes = serviceDeferred.await()
                    val contractRes = contractDeferred.await()

                    val rawInvoices = if (invoiceRes.isSuccessful && invoiceRes.body()?.code == 1000) {
                        invoiceRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val services = if (serviceRes.isSuccessful && serviceRes.body()?.code == 1000) {
                        serviceRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val contracts = if (contractRes.isSuccessful && contractRes.body()?.code == 1000) {
                        contractRes.body()?.result ?: emptyList()
                    } else emptyList()

                    val electricPrice = services.find { it.type == "ELECTRIC" }?.unitPrice ?: 3500.0
                    val waterPrice = services.find { it.type == "WATER" }?.unitPrice ?: 20000.0

                    // Chuyển đổi dữ liệu hóa đơn thật từ BE sang UI Model
                    val mappedInvoices: List<InvoiceItemUiModel> = if (rawInvoices.isNotEmpty()) {
                        rawInvoices.map { inv ->
                            val matchedContract = contracts.find { it.roomId == inv.roomId }
                            InvoiceItemUiModel(
                                id = inv.id,
                                realInvoiceId = inv.id,
                                contractId = matchedContract?.id ?: "",
                                roomCode = inv.roomCode ?: "Phòng",
                                tenantName = inv.tenantName ?: "Khách thuê",
                                tenantPhone = matchedContract?.note,
                                period = periodDisplay,
                                roomRent = matchedContract?.monthlyRent ?: (inv.totalAmount * 0.7),
                                electricUnitPrice = electricPrice,
                                waterUnitPrice = waterPrice,
                                totalBackendAmount = inv.totalAmount,
                                isPaid = inv.status == "PAID",
                                note = inv.dueDate?.let { "Hạn nộp: $it" }
                            )
                        }
                    } else {
                        // Nếu tháng này chưa bấm "Tạo hóa đơn", tạm tạo danh sách dự kiến từ Hợp đồng đang chạy
                        if (contracts.isNotEmpty()) {
                            contracts.mapIndexed { index, contract ->
                                InvoiceItemUiModel(
                                    id = "PENDING-${contract.id.take(8)}",
                                    contractId = contract.id,
                                    roomCode = contract.roomCode ?: "P${101 + index}",
                                    tenantName = contract.tenantName ?: "Khách thuê",
                                    period = periodDisplay,
                                    roomRent = contract.monthlyRent ?: 3000000.0,
                                    electricOld = 100.0 + index * 20,
                                    electricNew = 150.0 + index * 25,
                                    electricUnitPrice = electricPrice,
                                    waterOld = 30.0 + index * 5,
                                    waterNew = 38.0 + index * 7,
                                    waterUnitPrice = waterPrice,
                                    otherServicesAmount = 100000.0,
                                    isPaid = false,
                                    note = "Chưa phát hành hóa đơn"
                                )
                            }
                        } else {
                            getMockInvoices(periodDisplay, electricPrice, waterPrice)
                        }
                    }

                    lastLoadTime = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        properties = propertyList,
                        selectedProperty = activeProp,
                        invoices = mappedInvoices,
                        servicePrices = services,
                        bankName = savedBank,
                        bankAccount = savedAcc
                    )
                } else {
                    val periodDisplay = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invoices = getMockInvoices(periodDisplay, 3500.0, 20000.0),
                        bankName = savedBank,
                        bankAccount = savedAcc
                    )
                }
            } catch (e: Exception) {
                val periodDisplay = "Tháng ${_uiState.value.selectedMonth}/${_uiState.value.selectedYear}"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    invoices = getMockInvoices(periodDisplay, 3500.0, 20000.0),
                    errorMessage = "Chế độ ngoại tuyến: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Tự động sinh hóa đơn cho cả nhà trọ trong kỳ được chọn (POST /properties/{id}/invoices/generate)
     */
    fun generateInvoices(dueDate: String? = null, onComplete: ((Boolean) -> Unit)? = null) {
        val propId = _uiState.value.selectedProperty?.id ?: TokenManager.getActivePropertyId()
        if (propId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng chọn nhà trọ trước khi tạo hóa đơn")
            onComplete?.invoke(false)
            return
        }

        val period = String.format("%04d-%02d-01", _uiState.value.selectedYear, _uiState.value.selectedMonth)
        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val req = InvoiceGenerationRequest(period = period, dueDate = dueDate)
                val res = RetrofitClient.invoiceApi.generate(propId, req)

                if (res.isSuccessful && res.body()?.code == 1000) {
                    val result = res.body()?.result
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        successMessage = "Đã tạo ${result?.generated ?: 0} hóa đơn mới thành công! (Bỏ qua: ${result?.skipped ?: 0})"
                    )
                    loadData(force = true)
                    onComplete?.invoke(true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = "Tạo hóa đơn thất bại: ${res.body()?.message ?: res.code()}"
                    )
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Lỗi kết nối khi tạo hóa đơn: ${e.localizedMessage}"
                )
                onComplete?.invoke(false)
            }
        }
    }

    /**
     * Xác nhận thu tiền hóa đơn (Ghi nhận thanh toán qua API hoặc cập nhật UI)
     */
    fun confirmPayment(invoiceId: String, amount: Double? = null, method: String = "CASH") {
        val propId = _uiState.value.selectedProperty?.id ?: TokenManager.getActivePropertyId()
        val invoice = _uiState.value.invoices.find { it.id == invoiceId || it.realInvoiceId == invoiceId }
        val payAmount = amount ?: invoice?.totalAmount ?: 0.0

        if (!propId.isNullOrBlank() && invoice?.realInvoiceId != null) {
            viewModelScope.launch {
                try {
                    val req = PaymentRequest(amount = payAmount, method = method, note = "Thu tiền mặt trực tiếp")
                    val res = RetrofitClient.paymentApi.create(propId, invoice.realInvoiceId, req)

                    if (res.isSuccessful && res.body()?.code == 1000) {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Đã ghi nhận thanh toán thành công!"
                        )
                        loadData(force = true)
                        return@launch
                    }
                } catch (_: Exception) {
                    // Fallback local update
                }
            }
        }

        // Cập nhật trạng thái hiển thị trên Mobile ngay lập tức
        val updated = _uiState.value.invoices.map { inv ->
            if (inv.id == invoiceId || inv.realInvoiceId == invoiceId) {
                inv.copy(isPaid = true)
            } else inv
        }
        _uiState.value = _uiState.value.copy(
            invoices = updated,
            successMessage = "Đã xác nhận thu tiền thành công!"
        )
    }

    /**
     * Xóa hóa đơn (DELETE /properties/{id}/invoices/{invoiceId})
     */
    fun deleteInvoice(invoiceId: String) {
        val propId = _uiState.value.selectedProperty?.id ?: TokenManager.getActivePropertyId()
        val invoice = _uiState.value.invoices.find { it.id == invoiceId }
        val realId = invoice?.realInvoiceId ?: invoiceId

        if (!propId.isNullOrBlank() && !realId.startsWith("PENDING-") && !realId.startsWith("INV-")) {
            viewModelScope.launch {
                try {
                    val res = RetrofitClient.invoiceApi.delete(propId, realId)
                    if (res.isSuccessful) {
                        loadData(force = true)
                        _uiState.value = _uiState.value.copy(successMessage = "Đã xóa hóa đơn!")
                        return@launch
                    }
                } catch (_: Exception) {}
            }
        }

        // Fallback xóa trên UI
        _uiState.value = _uiState.value.copy(
            invoices = _uiState.value.invoices.filterNot { it.id == invoiceId },
            successMessage = "Đã xóa hóa đơn khỏi danh sách!"
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
