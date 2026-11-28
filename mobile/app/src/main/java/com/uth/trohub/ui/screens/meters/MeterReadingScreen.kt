package com.uth.trohub.ui.screens.meters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Ghi chỉ số Điện Nước (Yêu cầu 4)
 * - 2 Thẻ Card chính cho Điện và Nước
 * - Hiển thị "Chỉ số cũ" & TextField nhập "Chỉ số mới" (KeyboardType.Number)
 * - Tự động tính toán (Real-time): Số tiêu thụ = (Mới - Cũ) & Tiền tạm tính = Tiêu thụ * Đơn giá
 * - Nút bấm "Lưu chỉ số & Tạo hóa đơn"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterReadingScreen(
    viewModel: MeterReadingViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaveAndNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ghi Chỉ Số Điện Nước",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            // Nút bấm dưới đáy: "Lưu chỉ số & Tạo hóa đơn"
            Surface(
                color = White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveMeterAndGenerateInvoice { roomCode, total ->
                                Toast.makeText(
                                    context,
                                    "Đã lưu chỉ số phòng $roomCode! Hóa đơn: ${formatter.format(total)}",
                                    Toast.LENGTH_LONG
                                ).show()
                                onSaveAndNext()
                            }
                        },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Đang lưu chỉ số...", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                        } else {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = White)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Lưu Chỉ Số & Tạo Hóa Đơn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightMint)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. THANH CHỌN PHÒNG ĐANG THUÊ
            Text("Chọn phòng ghi chỉ số:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.rooms.forEachIndexed { index, room ->
                    val isSelected = index == uiState.selectedRoomIndex
                    StatusChip(
                        text = "Phòng ${room.roomCode} (${room.tenantName ?: "Trống"})",
                        backgroundColor = if (isSelected) PrimaryGreen else White,
                        textColor = if (isSelected) White else TextSecondary,
                        modifier = Modifier.clickable { viewModel.selectRoom(index) }
                    )
                }
            }

            val currentRoom = uiState.currentRoom

            // Thông tin phòng đang chọn
            if (currentRoom != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Phòng ${currentRoom.roomCode} • Khách: ${currentRoom.tenantName ?: "N/A"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Giá thuê phòng: ${formatter.format(currentRoom.monthlyRent)}/tháng",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // 2. THẺ CHÍNH 1: GHI CHỈ SỐ ĐIỆN & TÍNH TOÁN REAL-TIME
            MeterCard(
                title = "Điện sinh hoạt",
                unit = "kWh",
                icon = Icons.Default.Bolt,
                iconColor = Color(0xFFF59E0B), // Màu cam điện
                unitPrice = uiState.electricUnitPrice,
                previousReading = uiState.previousElectric,
                currentReadingInput = uiState.currentElectricInput,
                consumption = uiState.electricConsumption,
                temporaryCost = uiState.electricTemporaryCost,
                formatter = formatter,
                onInputChange = { viewModel.onElectricInputChange(it) }
            )

            // 3. THẺ CHÍNH 2: GHI CHỈ SỐ NƯỚC & TÍNH TOÁN REAL-TIME
            MeterCard(
                title = "Nước sinh hoạt",
                unit = "m³",
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFF3B82F6), // Màu xanh nước
                unitPrice = uiState.waterUnitPrice,
                previousReading = uiState.previousWater,
                currentReadingInput = uiState.currentWaterInput,
                consumption = uiState.waterConsumption,
                temporaryCost = uiState.waterTemporaryCost,
                formatter = formatter,
                onInputChange = { viewModel.onWaterInputChange(it) }
            )

            // 4. THẺ TỔNG HỢP CHI PHÍ DỊCH VỤ TẠM TÍNH
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tạm tính tiền dịch vụ kỳ này:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiền điện + Nước",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = try { formatter.format(uiState.totalServiceCost) } catch (_: Exception) { "${uiState.totalServiceCost} đ" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Thẻ Card nhập chỉ số có tính toán Real-time tự động
 */
@Composable
private fun MeterCard(
    title: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    unitPrice: Double,
    previousReading: Double,
    currentReadingInput: String,
    consumption: Double,
    temporaryCost: Double,
    formatter: NumberFormat,
    onInputChange: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header thẻ dịch vụ
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text("Đơn giá: ${formatter.format(unitPrice)} / $unit", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = LightMint.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))

            // Hàng hiển thị Chỉ số cũ & Ô nhập Chỉ số mới
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Chỉ số cũ (Readonly)
                Column(Modifier.weight(1f)) {
                    Text("Chỉ số cũ ($unit)", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(LightMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = previousReading.toInt().toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                    }
                }

                // Chỉ số mới (OutlinedTextField với KeyboardType.Number)
                Column(Modifier.weight(1.3f)) {
                    Text("Chỉ số mới ($unit) (*)", fontSize = 11.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = currentReadingInput,
                        onValueChange = onInputChange,
                        placeholder = { Text("Nhập số...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // KHUNG TÍNH TOÁN TỰ ĐỘNG (REAL-TIME DISPLAY)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightMint.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Số tiêu thụ", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${consumption.toInt()} $unit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Tiền tạm tính", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = try { formatter.format(temporaryCost) } catch (_: Exception) { "$temporaryCost đ" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryGreen
                    )
                }
            }
        }
    }
}
