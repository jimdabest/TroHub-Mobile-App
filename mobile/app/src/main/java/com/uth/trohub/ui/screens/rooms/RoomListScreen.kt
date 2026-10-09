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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

private data class Room(
    val name: String,
    val tenant: String,
    val rent: String,
    val noteTitle: String,
    val note: String,
    val action: String,
    val isAlert: Boolean = false
)

private val rooms = listOf(
    Room("Phòng 101", "Nguyễn Văn An", "3.200.000 đ", "Chỉ số kỳ này", "⚡ 428 kWh  💧16 m³", ""),
    Room("Phòng 102", "Trần Thị Bích", "3.000.000 đ", "Số nợ tồn đọng", "1.200.000 đ", "Nhắc nợ", true),
    Room("Phòng 103", "", "2.800.000 đ", "Sẵn sàng dọn vào", "P. 22m² • Đầy đủ tiện nghi", "Xếp khách"),
    Room("Phòng 201", "Lê Hoàng Long", "3.500.000 đ", "Hợp đồng", "Đến 12/2025", ""),
    Room("Phòng 202", "Phạm Minh Thu", "3.200.000 đ", "Số điện ghi nhận", "312 kWh (+78)", "Lập hóa đơn")
)

@Composable
fun RoomListScreen(
    onOpenMeterReading: () -> Unit = {},
    onOpenInvoices: () -> Unit = {}
) {
    var keyword by remember { mutableStateOf("") }
    val visibleRooms = rooms.filter { it.name.contains(keyword, true) || it.tenant.contains(keyword, true) }

    LazyColumn(
        modifier = Modifier.background(LightMint),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Khu nhà trọ Tro Hub Central", color = TextSecondary, fontSize = 12.sp)
            Text("Danh sách phòng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        item {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Tìm theo số phòng, tên khách...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White, unfocusedContainerColor = White,
                    focusedBorderColor = LightMint, unfocusedBorderColor = LightMint
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        item { RoomFilters() }
        item {
            Row {
                Text("DANH MỤC HIỂN THỊ", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.weight(1f))
                Text("${visibleRooms.size} phòng hiển thị", color = TextSecondary, fontSize = 10.sp)
            }
        }
        items(visibleRooms) { room -> RoomItem(room, onOpenMeterReading, onOpenInvoices) }
    }
}

@Composable
private fun RoomFilters() {
    val filters = listOf("Tất cả  20", "Đang thuê  18", "Trống  2", "Còn nợ  1")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEachIndexed { index, filter ->
            StatusChip(filter, if (index == 0) PrimaryGreen else White, if (index == 0) White else TextSecondary)
        }
    }
}

@Composable
private fun RoomItem(room: Room, onOpenMeterReading: () -> Unit, onOpenInvoices: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenMeterReading)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(room.name, color = PrimaryGreen, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (room.tenant.isNotBlank()) {
                        Text("♙ ${room.tenant}", color = TextPrimary, fontSize = 13.sp)
                    } else {
                        Text("P. 22m² • Đầy đủ tiện nghi", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Box(Modifier.size(34.dp).background(LightMint, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.BedroomParent, null, tint = PrimaryGreen, modifier = Modifier.size(19.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(LightMint.copy(alpha = .65f), RoundedCornerShape(8.dp)).padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Giá thuê cơ bản", color = TextSecondary, fontSize = 11.sp)
                    Text(room.rent, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(room.noteTitle, color = TextSecondary, fontSize = 11.sp)
                    Text(room.note, color = if (room.isAlert) AlertRed else PrimaryGreen, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                if (room.action.isNotBlank()) {
                    Box(Modifier.clickable(onClick = if (room.action == "Lập hóa đơn" || room.isAlert) onOpenInvoices else onOpenMeterReading)) {
                        StatusChip(room.action, if (room.isAlert) AlertBackground else LightMint, if (room.isAlert) AlertRed else PrimaryGreen)
                    }
                    Spacer(Modifier.width(9.dp))
                }
                if (room.tenant.isNotBlank()) {
                    Icon(Icons.Default.ChatBubbleOutline, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                } else {
                    Icon(Icons.Default.PersonAdd, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Box(Modifier.size(30.dp).background(LightMint, RoundedCornerShape(15.dp)).clickable(onClick = if (room.action == "Lập hóa đơn") onOpenInvoices else onOpenMeterReading), contentAlignment = Alignment.Center) {
                    Icon(if (room.action == "Lập hóa đơn") Icons.AutoMirrored.Filled.ReceiptLong else Icons.AutoMirrored.Filled.ArrowForward, null, tint = PrimaryGreen, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
