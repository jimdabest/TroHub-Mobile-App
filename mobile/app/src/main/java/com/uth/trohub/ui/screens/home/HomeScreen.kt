package com.uth.trohub.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.data.model.ContractResponse
import com.uth.trohub.data.model.PropertyResponse
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onOpenRooms: () -> Unit = {},
    onOpenTenants: () -> Unit = {},
    onOpenMeterReading: () -> Unit = {},
    onOpenRevenueReport: () -> Unit = {},
    onOpenInvoices: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreatePropertyDialog by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val formattedExpected = try {
        val total = if (uiState.totalExpectedRevenue > 0) uiState.totalExpectedRevenue else uiState.totalMonthlyRent
        formatter.format(total)
    } catch (_: Exception) {
        "${uiState.totalExpectedRevenue} đ"
    }
    val formattedRent = try {
        formatter.format(uiState.totalMonthlyRent)
    } catch (_: Exception) {
        "${uiState.totalMonthlyRent} đ"
    }
    val formattedUtilities = try {
        formatter.format(uiState.totalUtilityEstimate)
    } catch (_: Exception) {
        "${uiState.totalUtilityEstimate} đ"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightMint),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header chọn khu trọ
        item {
            PropertySelectorHeader(
                activeProperty = uiState.activeProperty,
                allProperties = uiState.properties,
                onPropertySelect = { viewModel.selectProperty(it) },
                onAddPropertyClick = { showCreatePropertyDialog = true }
            )
        }

        // Lỗi nếu có
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
                        TextButton(onClick = { viewModel.loadData() }) {
                            Text("Thử lại", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Banner quản lý thu tiền phòng
        item {
            PaymentBanner(
                activeContractsCount = uiState.activeContracts.size,
                formattedExpectedRevenue = formattedExpected,
                onOpenInvoices = onOpenInvoices
            )
        }

        // 3. Thẻ thống kê nhanh tình trạng phòng
        item {
            RoomOverviewCards(
                occupied = uiState.occupiedRooms,
                total = uiState.totalRooms,
                rate = uiState.occupancyRate,
                available = uiState.availableRooms
            )
        }

        // 4. Bốn phím tắt chức năng cốt lõi
        item {
            CoreFunctionShortcuts(
                onOpenRooms = onOpenRooms,
                onOpenTenants = onOpenTenants,
                onOpenMeterReading = onOpenMeterReading,
                onOpenInvoices = onOpenInvoices
            )
        }

        // 5. Thẻ tóm tắt tài chính & Tiền cọc
        item {
            FinancialSummaryCard(
                totalExpectedRevenue = formattedExpected,
                roomRentPart = formattedRent,
                utilitiesPart = formattedUtilities,
                totalDeposit = try { formatter.format(uiState.totalDeposit) } catch (_: Exception) { "${uiState.totalDeposit} đ" },
                occupancyRate = uiState.occupancyRate,
                activeCount = uiState.activeContracts.size,
                onOpenReport = onOpenRevenueReport
            )
        }

        // 6. Danh sách hợp đồng đang hiệu lực
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hợp đồng đang hoạt động",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${uiState.activeContracts.size} phòng đang thuê",
                    color = PrimaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState.activeContracts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Chưa có hợp đồng nào đang hoạt động.\nChuyển sang tab 'Phòng' và bấm 'Cho thuê' để tạo hợp đồng mới.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp).fillMaxWidth()
                    )
                }
            }
        } else {
            items(uiState.activeContracts) { contract ->
                ContractItemCard(contract)
            }
        }
    }

    if (showCreatePropertyDialog) {
        CreatePropertyDialog(
            onDismiss = { showCreatePropertyDialog = false },
            onConfirm = { name, address, desc ->
                viewModel.createProperty(name, address, desc) {
                    showCreatePropertyDialog = false
                }
            }
        )
    }
}

@Composable
private fun PropertySelectorHeader(
    activeProperty: PropertyResponse?,
    allProperties: List<PropertyResponse>,
    onPropertySelect: (PropertyResponse) -> Unit,
    onAddPropertyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(LightMint, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = PrimaryGreen)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "KHU TRỌ HIỆN TẠI",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = activeProperty?.name ?: "Chưa chọn khu trọ",
                        fontSize = 17.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!activeProperty?.address.isNullOrBlank()) {
                        Text(
                            text = activeProperty!!.address!!,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onAddPropertyClick) {
                    Icon(Icons.Default.AddBusiness, contentDescription = "Thêm khu trọ", tint = PrimaryGreen)
                }
            }

            // Nếu sở hữu nhiều khu trọ, hiển thị thanh cuộn chọn nhanh
            if (allProperties.size > 1) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allProperties.forEach { prop ->
                        val isSelected = prop.id == activeProperty?.id
                        StatusChip(
                            text = prop.name,
                            backgroundColor = if (isSelected) PrimaryGreen else LightMint,
                            textColor = if (isSelected) White else PrimaryGreen,
                            modifier = Modifier.clickable { onPropertySelect(prop) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentBanner(
    activeContractsCount: Int,
    formattedExpectedRevenue: String,
    onOpenInvoices: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenInvoices)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "QUẢN LÝ THU TRỌ THÁNG NÀY",
                    color = White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = if (activeContractsCount > 0)
                        "$activeContractsCount phòng thuê • Dự kiến: $formattedExpectedRevenue"
                    else
                        "Chưa có hợp đồng nào đang thuê",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusChip("Thu tiền", White, PrimaryGreen)
        }
    }
}

@Composable
private fun RoomOverviewCards(occupied: Int, total: Int, rate: Int, available: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OverviewMiniCard("Đang thuê", "$occupied/$total", "$rate% lấp đầy", PrimaryGreen, Modifier.weight(1f))
        OverviewMiniCard("Phòng trống", "$available", "Sẵn sàng", PrimaryGreen, Modifier.weight(1f))
        OverviewMiniCard("Tổng phòng", "$total", "Trong khu", TextSecondary, Modifier.weight(1f))
    }
}

@Composable
private fun OverviewMiniCard(
    title: String,
    mainValue: String,
    subValue: String,
    highlightColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(mainValue, color = highlightColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subValue, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun CoreFunctionShortcuts(
    onOpenRooms: () -> Unit,
    onOpenTenants: () -> Unit,
    onOpenMeterReading: () -> Unit,
    onOpenInvoices: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Lối tắt quản lý",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShortcutItem(Icons.Default.BedroomParent, "Phòng trọ", onOpenRooms)
                ShortcutItem(Icons.Default.People, "Khách thuê", onOpenTenants)
                ShortcutItem(Icons.Default.Bolt, "Ghi điện nước", onOpenMeterReading)
                ShortcutItem(Icons.Default.ReceiptLong, "Hóa đơn", onOpenInvoices)
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(LightMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryGreen, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FinancialSummaryCard(
    totalExpectedRevenue: String,
    roomRentPart: String,
    utilitiesPart: String,
    totalDeposit: String,
    occupancyRate: Int,
    activeCount: Int,
    onOpenReport: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tổng quan tài chính", color = TextSecondary, fontSize = 12.sp)
                    Text("Doanh thu dự kiến tháng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(onClick = onOpenReport) {
                    Text("Chi tiết →", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightMint.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Tổng tiền cần thu (gồm điện nước)", color = TextSecondary, fontSize = 11.sp)
                    Text(totalExpectedRevenue, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Phòng: $roomRentPart • Điện nước: $utilitiesPart", color = TextSecondary, fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tiền cọc đang giữ", color = TextSecondary, fontSize = 11.sp)
                    Text(totalDeposit, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ContractItemCard(contract: ContractResponse) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val formattedRent = try {
        formatter.format(contract.monthlyRent ?: 0.0)
    } catch (_: Exception) {
        "${contract.monthlyRent} đ"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(LightMint, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contract.roomCode ?: "P",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Phòng ${contract.roomCode ?: "N/A"} - ${contract.tenantName ?: "Khách thuê"}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Bắt đầu: ${contract.startDate} • Cọc: ${try { formatter.format(contract.deposit ?: 0.0) } catch (_: Exception) { "0đ" }}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = formattedRent,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun CreatePropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String?, desc: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Nhà Trọ Mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên nhà trọ (*)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Ghi chú / Mô tả") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), address.trim().ifEmpty { null }, desc.trim().ifEmpty { null })
                    }
                },
                enabled = name.isNotBlank()
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
