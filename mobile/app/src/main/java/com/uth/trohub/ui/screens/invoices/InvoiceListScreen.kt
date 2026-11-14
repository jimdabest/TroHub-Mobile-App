package com.uth.trohub.ui.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.data.model.PropertyResponse
import com.uth.trohub.data.model.ServicePriceResponse
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Danh sách Hóa đơn & Lọc dữ liệu (Yêu cầu 1 & 2)
 * - Filter Bar chọn Nhà trọ & Month Picker (Tháng/Năm)
 * - Nút "Sửa biểu giá" mở BottomSheet chỉnh sửa đơn giá
 * - Danh sách thẻ hóa đơn các phòng
 * - Bấm vào hóa đơn sẽ mở màn hình Chi tiết hóa đơn (Yêu cầu 3)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Quản lý trạng thái mở Chi tiết hóa đơn
    var selectedInvoiceForDetail by remember { mutableStateOf<InvoiceItemUiModel?>(null) }

    // Quản lý BottomSheet chỉnh sửa biểu giá dịch vụ
    var showEditServicePriceSheet by remember { mutableStateOf(false) }

    // Nếu đang xem chi tiết hóa đơn, hiển thị InvoiceDetailScreen
    if (selectedInvoiceForDetail != null) {
        InvoiceDetailScreen(
            invoice = selectedInvoiceForDetail!!,
            bankName = uiState.bankName,
            bankAccount = uiState.bankAccount,
            accountOwner = uiState.accountOwner,
            onBackClick = { selectedInvoiceForDetail = null },
            onConfirmPayment = { invoiceId ->
                viewModel.confirmPayment(invoiceId)
                selectedInvoiceForDetail = selectedInvoiceForDetail?.copy(isPaid = true)
            }
        )
        return
    }

    // Lọc danh sách theo trạng thái (Tất cả / Chờ thanh toán / Đã thu)
    val filteredInvoices = uiState.invoices.filter { invoice ->
        when (uiState.statusFilter) {
            "UNPAID" -> !invoice.isPaid
            "PAID" -> invoice.isPaid
            else -> true
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightMint),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. THANH CÔNG CỤ LỌC (Filter Bar: Chọn Nhà Trọ & Chọn Tháng/Năm)
            item {
                InvoiceFilterBar(
                    properties = uiState.properties,
                    selectedProperty = uiState.selectedProperty,
                    selectedMonth = uiState.selectedMonth,
                    selectedYear = uiState.selectedYear,
                    onSelectProperty = { viewModel.selectProperty(it) },
                    onSelectMonthYear = { m, y -> viewModel.selectMonthYear(m, y) },
                    onOpenEditPrices = { showEditServicePriceSheet = true }
                )
            }

            // 2. Thẻ Tóm tắt Thu Chi trong kỳ
            item {
                InvoiceSummaryCard(
                    totalExpected = uiState.totalExpectedRevenue,
                    totalCollected = uiState.totalCollectedRevenue,
                    paidCount = uiState.paidCount,
                    totalCount = uiState.invoices.size,
                    formatter = formatter
                )
            }

            // 3. Bộ lọc trạng thái (Status Chips)
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Triple("ALL", "Tất cả (${uiState.invoices.size})", uiState.statusFilter == "ALL"),
                        Triple("UNPAID", "Chờ thanh toán (${uiState.invoices.size - uiState.paidCount})", uiState.statusFilter == "UNPAID"),
                        Triple("PAID", "Đã thu tiền (${uiState.paidCount})", uiState.statusFilter == "PAID")
                    )
                    filters.forEach { (key, label, isSelected) ->
                        StatusChip(
                            text = label,
                            backgroundColor = if (isSelected) PrimaryGreen else White,
                            textColor = if (isSelected) White else TextSecondary,
                            modifier = Modifier.clickable { viewModel.setStatusFilter(key) }
                        )
                    }
                }
            }

            // Thông báo lỗi nếu có
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AlertBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = AlertRed,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.loadData(force = true) }) {
                                Text("Thử lại", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. DANH SÁCH THẺ HÓA ĐƠN CÁC PHÒNG (Áp dụng Skeleton Loading)
            if (uiState.isLoading) {
                items(3) {
                    com.uth.trohub.ui.components.SkeletonInvoiceCard()
                }
            } else if (filteredInvoices.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Không có hóa đơn nào phù hợp với bộ lọc.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredInvoices) { invoice ->
                    InvoiceRoomCard(
                        invoice = invoice,
                        formatter = formatter,
                        onClick = { selectedInvoiceForDetail = invoice },
                        onQuickPay = { viewModel.confirmPayment(invoice.id) }
                    )
                }
            }
        }
    }

    // 5. GIAO DIỆN CHỈNH SỬA BIỂU GIÁ DỊCH VỤ (BottomSheet - Yêu cầu 2)
    if (showEditServicePriceSheet) {
        EditServicePriceBottomSheet(
            servicePrices = uiState.servicePrices,
            formatter = formatter,
            onDismiss = { showEditServicePriceSheet = false },
            onSaveServicePrice = { serviceId, name, type, unit, price ->
                viewModel.updateServicePrice(serviceId, name, type, unit, price)
            }
        )
    }
}

/**
 * 1. Thanh công cụ lọc: Dropdown chọn Nhà Trọ & Month Picker chọn Tháng/Năm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceFilterBar(
    properties: List<PropertyResponse>,
    selectedProperty: PropertyResponse?,
    selectedMonth: Int,
    selectedYear: Int,
    onSelectProperty: (PropertyResponse) -> Unit,
    onSelectMonthYear: (Int, Int) -> Unit,
    onOpenEditPrices: () -> Unit
) {
    var expandedPropDropdown by remember { mutableStateOf(false) }
    var showMonthPickerDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown chọn Nhà trọ
                ExposedDropdownMenuBox(
                    expanded = expandedPropDropdown,
                    onExpandedChange = { expandedPropDropdown = !expandedPropDropdown },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = selectedProperty?.name ?: "Khu nhà trọ",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Khu trọ", fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPropDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPropDropdown,
                        onDismissRequest = { expandedPropDropdown = false }
                    ) {
                        properties.forEach { prop ->
                            DropdownMenuItem(
                                text = { Text(prop.name, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    onSelectProperty(prop)
                                    expandedPropDropdown = false
                                }
                            )
                        }
                    }
                }

                // Month Picker (Nút mở dialog chọn Tháng/Năm)
                OutlinedButton(
                    onClick = { showMonthPickerDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = LightMint.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "T$selectedMonth/$selectedYear",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Nút mở cấu hình giá dịch vụ
                IconButton(
                    onClick = onOpenEditPrices,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(46.dp)
                        .background(LightMint, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Sửa biểu giá", tint = PrimaryGreen)
                }
            }
        }
    }

    // Dialog chọn Tháng & Năm
    if (showMonthPickerDialog) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthPickerDialog = false },
            onConfirm = { m, y ->
                onSelectMonthYear(m, y)
                showMonthPickerDialog = false
            }
        )
    }
}

/**
 * Thẻ hiển thị tóm tắt doanh thu dự kiến & thực tế
 */
@Composable
private fun InvoiceSummaryCard(
    totalExpected: Double,
    totalCollected: Double,
    paidCount: Int,
    totalCount: Int,
    formatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "TỔNG TIỀN PHÒNG & DỊCH VỤ CẦN THU",
                color = White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = try { formatter.format(totalExpected) } catch (_: Exception) { "$totalExpected đ" },
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã thu: ${try { formatter.format(totalCollected) } catch (_: Exception) { "$totalCollected đ" }}",
                    color = White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Tiến độ: $paidCount/$totalCount phòng",
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Thẻ hiển thị một hóa đơn trong danh sách (dạng Card sắc nét)
 */
@Composable
private fun InvoiceRoomCard(
    invoice: InvoiceItemUiModel,
    formatter: NumberFormat,
    onClick: () -> Unit,
    onQuickPay: () -> Unit
) {
    val formattedTotal = try {
        formatter.format(invoice.totalAmount)
    } catch (_: Exception) {
        "${invoice.totalAmount} đ"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(LightMint, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = invoice.roomCode,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Phòng ${invoice.roomCode} - ${invoice.tenantName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tiền phòng: ${formatter.format(invoice.roomRent)} • Điện: ${invoice.electricUsage.toInt()} kWh",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                StatusChip(
                    text = if (invoice.isPaid) "✓ Đã thu" else "Chưa thu",
                    backgroundColor = if (invoice.isPaid) LightMint else AlertBackground,
                    textColor = if (invoice.isPaid) PrimaryGreen else AlertRed
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = LightMint.copy(alpha = 0.6f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng cần thu", fontSize = 11.sp, color = TextSecondary)
                    Text(formattedTotal, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryGreen)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!invoice.isPaid) {
                        OutlinedButton(
                            onClick = onQuickPay,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Thu nhanh", fontSize = 11.sp, color = PrimaryGreen)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Xem chi tiết",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 2. Giao diện Chỉnh sửa biểu giá dịch vụ (BottomSheet - Yêu cầu 2)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditServicePriceBottomSheet(
    servicePrices: List<ServicePriceResponse>,
    formatter: NumberFormat,
    onDismiss: () -> Unit,
    onSaveServicePrice: (serviceId: String, name: String, type: String, unit: String, price: Double) -> Unit
) {
    // Biểu giá mẫu mặc định nếu backend chưa có
    val displayPrices = if (servicePrices.isNotEmpty()) servicePrices else listOf(
        ServicePriceResponse("sp-1", "", "Điện sinh hoạt", "ELECTRIC", "kWh", 3500.0, true),
        ServicePriceResponse("sp-2", "", "Nước sinh hoạt", "WATER", "m³", 20000.0, true),
        ServicePriceResponse("sp-3", "", "Rác & Vệ sinh", "FIXED", "tháng", 50000.0, true),
        ServicePriceResponse("sp-4", "", "Wifi Internet", "FIXED", "phòng/tháng", 100000.0, true)
    )

    var editingPrice by remember { mutableStateOf<ServicePriceResponse?>(null) }
    var inputPriceStr by remember { mutableStateOf("") }
    var inputUnit by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cài Đặt Biểu Giá Dịch Vụ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Text(
                text = "Chạm vào một dịch vụ để điều chỉnh đơn giá tính toán cho hóa đơn.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            // Danh sách các dịch vụ hiện có
            displayPrices.forEach { service ->
                val isEditing = editingPrice?.id == service.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEditing) LightMint else White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingPrice = service
                            inputPriceStr = service.unitPrice.toLong().toString()
                            inputUnit = service.unit ?: ""
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(service.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Đơn vị: ${service.unit ?: "Lần"} • Loại: ${service.type}", fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            text = formatter.format(service.unitPrice),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryGreen
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Khung nhập đơn giá mới khi chọn một dịch vụ
            if (editingPrice != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightMint.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Chỉnh sửa: ${editingPrice!!.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryGreen
                        )

                        OutlinedTextField(
                            value = inputPriceStr,
                            onValueChange = { inputPriceStr = it },
                            label = { Text("Đơn giá mới (VNĐ)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = inputUnit,
                            onValueChange = { inputUnit = it },
                            label = { Text("Đơn vị tính (kWh, m³, tháng...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val newP = inputPriceStr.toDoubleOrNull() ?: editingPrice!!.unitPrice
                                    val unitToSave = inputUnit.trim().ifEmpty { editingPrice!!.unit ?: "Lần" }
                                    onSaveServicePrice(
                                        editingPrice!!.id,
                                        editingPrice!!.name,
                                        editingPrice!!.type,
                                        unitToSave,
                                        newP
                                    )
                                    editingPrice = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("Lưu / Cập Nhật")
                            }

                            OutlinedButton(
                                onClick = { editingPrice = null },
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Hủy")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

/**
 * Dialog chọn Tháng & Năm đơn giản và trực quan
 */
@Composable
private fun MonthYearPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedYear by remember { mutableStateOf(currentYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn Tháng & Năm", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Chọn năm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }
                    Text("Năm $selectedYear", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                // Lưới 12 tháng
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0..3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (col in 1..3) {
                                val monthNum = row * 3 + col
                                val isSelected = monthNum == selectedMonth
                                Button(
                                    onClick = { selectedMonth = monthNum },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) PrimaryGreen else LightMint,
                                        contentColor = if (isSelected) White else TextPrimary
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Th $monthNum", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMonth, selectedYear) }) {
                Text("Chọn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
