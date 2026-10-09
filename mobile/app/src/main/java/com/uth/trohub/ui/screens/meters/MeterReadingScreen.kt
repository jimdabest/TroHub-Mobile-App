package com.uth.trohub.ui.screens.meters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.components.AppTopBar
import com.uth.trohub.ui.components.PrimaryButton
import com.uth.trohub.ui.theme.BackgroundGray
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

private data class MeterRoom(
    val area: String = "Dãy A - Tầng 1",
    val position: String = "Phòng 1 / 10",
    val roomName: String = "Phòng 101",
    val tenant: String = "Nguyễn Văn An",
    val dueDate: String = "Ngày 15 hàng tháng"
)

private val meterRoom = MeterRoom()

@Composable
fun MeterReadingScreen(
    onBackClick: () -> Unit = {},
    onSaveAndNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var electricityReading by remember { mutableStateOf("1458") }
    var waterReading by remember { mutableStateOf("86") }

    Scaffold(
        topBar = { AppTopBar(title = "Ghi Điện Nước", onBackClick = onBackClick) },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(White).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(text = "Lưu & Sang phòng kế tiếp  →", onClick = onSaveAndNext)
                TextButton(onClick = onSkip) {
                    Text("Bỏ qua phòng này", color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }
            }
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RoomHeader(meterRoom)
            MeterInputCard(
                icon = Icons.Default.Bolt,
                title = "Chỉ số điện mới (kWh)",
                unitPrice = "Đơn giá: 3.500 đ/kWh",
                value = electricityReading,
                onValueChange = { electricityReading = it },
                previous = "Kỳ trước: 1342 kWh",
                usage = "Đã dùng: 116 kWh"
            )
            MeterInputCard(
                icon = Icons.Default.WaterDrop,
                title = "Chỉ số nước mới (m³)",
                unitPrice = "Đơn giá: 18.000 đ/m³",
                value = waterReading,
                onValueChange = { waterReading = it },
                previous = "Kỳ trước: 74 m³",
                usage = "Đã dùng: 12 m³"
            )
            EstimatedUtilityCard()
            RentalInformationCard()
            ServiceCard()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RoomHeader(room: MeterRoom) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(room.area, color = PrimaryGreen, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(room.position, color = PrimaryGreen, fontSize = 12.sp, modifier = Modifier.background(LightMint, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(room.roomName, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(room.tenant, color = TextPrimary, fontSize = 14.sp)
            Spacer(Modifier.height(5.dp))
            Text("Hạn chốt: ${room.dueDate}", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MeterInputCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    unitPrice: String,
    value: String,
    onValueChange: (String) -> Unit,
    previous: String,
    usage: String
) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(LightMint, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(unitPrice, color = TextSecondary, fontSize = 10.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(if (title.contains("điện")) "kWh" else "m³", color = TextSecondary, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LightMint,
                        unfocusedContainerColor = LightMint,
                        focusedBorderColor = LightMint,
                        unfocusedBorderColor = LightMint
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(56.dp).background(LightMint, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, "Chụp ảnh chỉ số", tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        Text("Chụp ảnh", color = PrimaryGreen, fontSize = 9.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(BackgroundGray, RoundedCornerShape(7.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(previous, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(usage, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EstimatedUtilityCard() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("▣ Tạm tính tiện ích", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("622.000 đ", color = PrimaryGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("(116 kWh × 3.500đ) + (12 m³ × 18.000đ)", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RentalInformationCard() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("▤ Hợp đồng thuê", color = TextPrimary, fontWeight = FontWeight.Bold)
            InfoLine("Giá thuê cơ bản", "3.000.000 đ/tháng")
            InfoLine("Tiền cọc phòng", "1.000.000 đ")
            InfoLine("Thời hạn hợp đồng", "15/12/2026")
            InfoLine("Kỳ hạn thanh toán", "Ngày 05 hàng tháng")
        }
    }
}

@Composable
private fun ServiceCard() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("☷ Dịch vụ & Tiện ích", color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ServiceTag("⚡ Điện", "3.500 đ", "kWh", Modifier.weight(1f))
                ServiceTag("♢ Nước", "25.000 đ", "m³", Modifier.weight(1f))
                ServiceTag("◉ Mạng & Rác", "0 đ", "Miễn phí", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ServiceTag(title: String, value: String, unit: String, modifier: Modifier) {
    Column(modifier.background(BackgroundGray, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(title, color = TextPrimary, fontSize = 11.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(unit, color = TextSecondary, fontSize = 10.sp)
    }
}
