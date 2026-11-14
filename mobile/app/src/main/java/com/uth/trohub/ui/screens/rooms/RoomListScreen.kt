package com.uth.trohub.ui.screens.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.data.model.TenantResponse
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun RoomListScreen(
    viewModel: RoomViewModel = viewModel(),
    onOpenMeterReading: () -> Unit = {},
    onOpenInvoices: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var rentingRoom by remember { mutableStateOf<RoomItemUiModel?>(null) }
    var terminatingRoom by remember { mutableStateOf<RoomItemUiModel?>(null) }

    val filteredRooms = uiState.rooms.filter { room ->
        val matchesFilter = when (uiState.selectedFilter) {
            "OCCUPIED" -> room.status == "OCCUPIED"
            "AVAILABLE" -> room.status == "AVAILABLE"
            "MAINTENANCE" -> room.status == "MAINTENANCE"
            else -> true
        }
        val matchesQuery = uiState.searchQuery.isBlank() ||
                room.code.contains(uiState.searchQuery, ignoreCase = true) ||
                (room.tenantName?.contains(uiState.searchQuery, ignoreCase = true) == true)
        matchesFilter && matchesQuery
    }

    val occupiedCount = uiState.rooms.count { it.status == "OCCUPIED" }
    val availableCount = uiState.rooms.count { it.status == "AVAILABLE" }
    val maintenanceCount = uiState.rooms.count { it.status == "MAINTENANCE" }

    LazyColumn(
        modifier = Modifier.background(LightMint),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = uiState.propertyName.ifEmpty { "Khu nhà trọ" },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text("Danh sách phòng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm phòng", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm phòng", fontSize = 12.sp)
                }
            }
        }

        // Ô tìm kiếm
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Tìm theo số phòng hoặc tên khách...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                singleLine = true
            )
        }

        // Lọc trạng thái
        item {
            RoomFilters(
                selectedFilter = uiState.selectedFilter,
                totalCount = uiState.rooms.size,
                occupiedCount = occupiedCount,
                availableCount = availableCount,
                maintenanceCount = maintenanceCount,
                onSelectFilter = { viewModel.setFilter(it) }
            )
        }

        // Hiển thị thông báo lỗi nếu có
        if (uiState.errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Đóng", fontSize = 12.sp, color = AlertRed)
                        }
                    }
                }
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
        } else if (filteredRooms.isEmpty()) {
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
                            text = if (uiState.rooms.isEmpty())
                                "Chưa có phòng nào trong nhà trọ này.\nBấm 'Thêm phòng' để bắt đầu tạo!"
                            else
                                "Không tìm thấy phòng phù hợp với bộ lọc.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredRooms) { room ->
                RoomCard(
                    room = room,
                    isToggling = uiState.togglingRoomId == room.id,
                    onOpenMeterReading = onOpenMeterReading,
                    onToggleStatus = { viewModel.toggleRoomStatus(room.id, room.status) },
                    onRentRoom = { rentingRoom = room },
                    onTerminateContract = { terminatingRoom = room }
                )
            }
        }
    }

    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { code, price, block, floor, area, desc ->
                viewModel.createRoom(code, price, block, floor, area, desc) {
                    showAddDialog = false
                }
            }
        )
    }

    if (rentingRoom != null) {
        RentRoomDialog(
            room = rentingRoom!!,
            existingTenants = uiState.tenants,
            onDismiss = { rentingRoom = null },
            onConfirmExisting = { tenantId, startDate, deposit, rent, note ->
                viewModel.createContract(
                    roomId = rentingRoom!!.id,
                    tenantId = tenantId,
                    startDate = startDate,
                    deposit = deposit,
                    monthlyRent = rent,
                    note = note
                ) {
                    rentingRoom = null
                }
            },
            onConfirmNew = { fullName, phone, idNumber, startDate, deposit, rent ->
                viewModel.createTenantAndContract(
                    roomId = rentingRoom!!.id,
                    fullName = fullName,
                    phone = phone,
                    idNumber = idNumber,
                    startDate = startDate,
                    deposit = deposit,
                    monthlyRent = rent
                ) {
                    rentingRoom = null
                }
            }
        )
    }

    if (terminatingRoom != null) {
        AlertDialog(
            onDismissRequest = { terminatingRoom = null },
            title = { Text("Thanh lý hợp đồng", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Bạn có chắc muốn thanh lý hợp đồng của phòng ${terminatingRoom!!.code} " +
                            "(Khách: ${terminatingRoom!!.tenantName ?: "N/A"})? " +
                            "Phòng sẽ tự động chuyển về trạng thái Trống."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val contractId = terminatingRoom!!.contractId
                        if (contractId != null) {
                            viewModel.terminateContract(contractId) {
                                terminatingRoom = null
                            }
                        } else {
                            terminatingRoom = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Xác nhận thanh lý")
                }
            },
            dismissButton = {
                TextButton(onClick = { terminatingRoom = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun RoomFilters(
    selectedFilter: String,
    totalCount: Int,
    occupiedCount: Int,
    availableCount: Int,
    maintenanceCount: Int,
    onSelectFilter: (String) -> Unit
) {
    val filters = listOf(
        Pair("ALL", "Tất cả  $totalCount"),
        Pair("OCCUPIED", "Đang thuê  $occupiedCount"),
        Pair("AVAILABLE", "Trống  $availableCount"),
        Pair("MAINTENANCE", "Bảo trì  $maintenanceCount")
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (key, label) ->
            val isSelected = selectedFilter == key
            StatusChip(
                text = label,
                backgroundColor = if (isSelected) PrimaryGreen else White,
                textColor = if (isSelected) White else TextSecondary,
                modifier = Modifier.clickable { onSelectFilter(key) }
            )
        }
    }
}

@Composable
private fun RoomCard(
    room: RoomItemUiModel,
    isToggling: Boolean = false,
    onOpenMeterReading: () -> Unit,
    onToggleStatus: () -> Unit,
    onRentRoom: () -> Unit,
    onTerminateContract: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val formattedPrice = try {
        formatter.format(room.price)
    } catch (_: Exception) {
        "${room.price} đ"
    }

    val (statusLabel, statusBg, statusColor) = when (room.status) {
        "OCCUPIED" -> Triple("Đang thuê", LightMint, PrimaryGreen)
        "AVAILABLE" -> Triple("Trống", LightMint, PrimaryGreen)
        "MAINTENANCE" -> Triple("Bảo trì", AlertBackground, AlertRed)
        else -> Triple(room.status, LightMint, PrimaryGreen)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Phòng ${room.code}",
                            color = PrimaryGreen,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusChip(
                            text = statusLabel,
                            backgroundColor = statusBg,
                            textColor = statusColor
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    if (!room.tenantName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Khách: ${room.tenantName}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val subText = listOfNotNull(
                            room.block?.let { "Khu $it" },
                            room.floor?.let { "Tầng $it" },
                            room.areaM2?.let { "${it.toInt()}m²" }
                        ).joinToString(" • ")
                        Text(
                            text = subText.ifEmpty { "Chưa có khách thuê" },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Box(
                    Modifier.size(34.dp).background(LightMint, RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.BedroomParent, null, tint = PrimaryGreen, modifier = Modifier.size(19.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightMint.copy(alpha = .65f), RoundedCornerShape(8.dp))
                    .padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Giá thuê cơ bản", color = TextSecondary, fontSize = 11.sp)
                    Text(formattedPrice, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                if (!room.description.isNullOrBlank()) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Ghi chú", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            room.description,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Trạng thái AVAILABLE: Cho phép Bảo trì HOẶC Tạo Hợp đồng cho thuê
                if (room.status == "AVAILABLE") {
                    TextButton(
                        onClick = onToggleStatus,
                        enabled = !isToggling,
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isToggling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = AlertRed
                            )
                        } else {
                            Icon(Icons.Default.Build, null, modifier = Modifier.size(14.dp), tint = AlertRed)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (isToggling) "Đang lưu..." else "Bảo trì", fontSize = 11.sp, color = AlertRed)
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onRentRoom,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cho thuê", fontSize = 11.sp)
                    }
                } else if (room.status == "MAINTENANCE") {
                    TextButton(
                        onClick = onToggleStatus,
                        enabled = !isToggling,
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isToggling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryGreen
                            )
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = PrimaryGreen)
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(if (isToggling) "Đang lưu..." else "Đưa vào sử dụng", fontSize = 11.sp, color = PrimaryGreen)
                    }
                } else if (room.status == "OCCUPIED") {
                    // Phòng đang có khách thuê: Cho phép thanh lý hợp đồng
                    OutlinedButton(
                        onClick = onTerminateContract,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(13.dp), tint = AlertRed)
                        Spacer(Modifier.width(4.dp))
                        Text("Thanh lý HĐ", fontSize = 11.sp, color = AlertRed)
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .size(30.dp)
                        .background(LightMint, RoundedCornerShape(15.dp))
                        .clickable(onClick = onOpenMeterReading),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Xem chỉ số",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, price: Double, block: String?, floor: Int?, area: Double?, desc: String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var floorStr by remember { mutableStateOf("") }
    var areaStr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Phòng Mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Mã phòng (VD: 101, 102) (*)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Giá thuê (VNĐ/tháng) (*)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = block,
                        onValueChange = { block = it },
                        label = { Text("Dãy / Khu") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = floorStr,
                        onValueChange = { floorStr = it },
                        label = { Text("Tầng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = areaStr,
                    onValueChange = { areaStr = it },
                    label = { Text("Diện tích (m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mô tả / Tiện ích") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    if (code.isNotBlank() && price > 0) {
                        onConfirm(
                            code.trim(),
                            price,
                            block.trim().ifEmpty { null },
                            floorStr.toIntOrNull(),
                            areaStr.toDoubleOrNull(),
                            desc.trim().ifEmpty { null }
                        )
                    }
                },
                enabled = code.isNotBlank() && (priceStr.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Tạo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentRoomDialog(
    room: RoomItemUiModel,
    existingTenants: List<TenantResponse>,
    onDismiss: () -> Unit,
    onConfirmExisting: (tenantId: String, startDate: String, deposit: Double?, rent: Double?, note: String?) -> Unit,
    onConfirmNew: (fullName: String, phone: String?, idNumber: String?, startDate: String, deposit: Double?, rent: Double?) -> Unit
) {
    var isNewTenant by remember { mutableStateOf(existingTenants.isEmpty()) }
    var selectedTenant by remember { mutableStateOf(existingTenants.firstOrNull()) }
    var expandedTenantDropdown by remember { mutableStateOf(false) }

    // Thông tin khách mới
    var newFullName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newIdNumber by remember { mutableStateOf("") }

    // Thông tin hợp đồng
    val todayStr = remember { LocalDate.now().toString() }
    var startDate by remember { mutableStateOf(todayStr) }
    var rentStr by remember { mutableStateOf(room.price.toLong().toString()) }
    var depositStr by remember { mutableStateOf((room.price.toLong()).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ký Hợp Đồng Thuê - Phòng ${room.code}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existingTenants.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isNewTenant = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isNewTenant) PrimaryGreen else LightMint,
                                contentColor = if (!isNewTenant) White else TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Khách có sẵn", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { isNewTenant = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNewTenant) PrimaryGreen else LightMint,
                                contentColor = if (isNewTenant) White else TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Khách mới", fontSize = 12.sp)
                        }
                    }
                }

                if (!isNewTenant && existingTenants.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTenantDropdown,
                        onExpandedChange = { expandedTenantDropdown = !expandedTenantDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedTenant?.fullName ?: "Chọn khách thuê",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chọn khách thuê (*)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTenantDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTenantDropdown,
                            onDismissRequest = { expandedTenantDropdown = false }
                        ) {
                            existingTenants.forEach { tenant ->
                                DropdownMenuItem(
                                    text = { Text("${tenant.fullName} (${tenant.phone ?: "Không SĐT"})") },
                                    onClick = {
                                        selectedTenant = tenant
                                        expandedTenantDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = newFullName,
                        onValueChange = { newFullName = it },
                        label = { Text("Họ tên khách (*)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Số điện thoại") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newIdNumber,
                        onValueChange = { newIdNumber = it },
                        label = { Text("CCCD / CMND") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Ngày bắt đầu (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentStr,
                    onValueChange = { rentStr = it },
                    label = { Text("Tiền thuê thỏa thuận (đ/tháng)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = depositStr,
                    onValueChange = { depositStr = it },
                    label = { Text("Tiền cọc giữ phòng (đ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rent = rentStr.toDoubleOrNull()
                    val deposit = depositStr.toDoubleOrNull()
                    if (isNewTenant) {
                        if (newFullName.isNotBlank() && startDate.isNotBlank()) {
                            onConfirmNew(
                                newFullName.trim(),
                                newPhone.trim().ifEmpty { null },
                                newIdNumber.trim().ifEmpty { null },
                                startDate.trim(),
                                deposit,
                                rent
                            )
                        }
                    } else {
                        selectedTenant?.let {
                            onConfirmExisting(it.id, startDate.trim(), deposit, rent, null)
                        }
                    }
                },
                enabled = if (isNewTenant) newFullName.isNotBlank() && startDate.isNotBlank() else selectedTenant != null
            ) {
                Text("Ký hợp đồng")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
