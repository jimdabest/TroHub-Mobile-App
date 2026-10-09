package com.uth.trohub.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private data class Reminder(
    val room: String,
    val detail: String,
    val timeLabel: String,
    val urgent: Boolean
)

private data class RevenuePoint(val month: String, val amount: String, val height: Int)

private val homeReminders = listOf(
    Reminder(
        room = "Phòng 204 – Dãy B",
        detail = "Hết hạn hợp đồng thuê (Khách: Nguyễn Văn An). Hãy liên hệ gia hạn hoặc tìm khách mới.",
        timeLabel = "5 ngày nữa",
        urgent = true
    ),
    Reminder(
        room = "Phòng 102 – Dãy A",
        detail = "Đã gửi thông báo chỉ số điện nước thành công qua tin nhắn Zalo.",
        timeLabel = "Hôm nay",
        urgent = false
    )
)

private val revenue = listOf(
    RevenuePoint("Th 5", "19.5", 88), RevenuePoint("Th 6", "20.2", 100),
    RevenuePoint("Th 7", "21.0", 106), RevenuePoint("Th 8", "20.8", 103),
    RevenuePoint("Th 9", "21.5", 110), RevenuePoint("Th 10", "22.0", 120)
)

@Composable
fun HomeScreen(
    onOpenMeterReading: () -> Unit = {},
    onOpenRevenueReport: () -> Unit = {},
    onOpenInvoices: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.background(LightMint),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PaymentNotice(onOpenInvoices) }
        item { OverviewCards() }
        item { UtilityShortcuts(onOpenMeterReading, onOpenRevenueReport, onOpenInvoices) }
        item { RevenueChart() }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nhắc nhở & Hoạt động", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text("Tất cả", color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        items(homeReminders) { reminder -> ReminderItem(reminder) }
        item { RentalSummary() }
    }
}

@Composable
private fun PaymentNotice(onOpenInvoices: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable(onClick = onOpenInvoices)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("KỲ THANH TOÁN THÁNG 10", color = White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("8 phòng đang chờ xác nhận\nchuyển khoản", color = White, fontSize = 14.sp)
            }
            StatusChip("Xem ngay", White, PrimaryGreen)
        }
    }
}

@Composable
private fun OverviewCards() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OverviewCard("Đang thuê", "18/20 phòng", "92% lấp đầy", false, Modifier.weight(1f))
        OverviewCard("Cần thu", "15.500.000 đ", "Kỳ tháng 10", false, Modifier.weight(1f))
        OverviewCard("Tổng còn nợ", "3.200.000 đ", "1 phòng quá hạn", true, Modifier.weight(1f))
    }
}

@Composable
private fun OverviewCard(title: String, value: String, caption: String, alert: Boolean, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = if (alert) AlertRed else TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(7.dp))
            Text(value, color = if (alert) AlertRed else PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(caption, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun UtilityShortcuts(
    onOpenMeterReading: () -> Unit,
    onOpenRevenueReport: () -> Unit,
    onOpenInvoices: () -> Unit
) {
    val shortcuts = listOf(
        Triple(Icons.Default.Bolt, "Điện nước", ""), Triple(Icons.Default.CalendarMonth, "Hóa đơn", ""),
        Triple(Icons.Default.Payments, "Thu tiền", ""), Triple(Icons.Default.PersonAdd, "Thêm khách", ""),
        Triple(Icons.AutoMirrored.Filled.TrendingUp, "Doanh thu", "")
    )
    Column {
        Text("Tiện ích", color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val noShortcutAction: () -> Unit = {}
                shortcuts.forEachIndexed { index, (icon, label) ->
                    val onClick: () -> Unit = when (index) {
                        0 -> onOpenMeterReading
                        1, 2 -> onOpenInvoices
                        4 -> onOpenRevenueReport
                        else -> noShortcutAction
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
                        Box(Modifier.size(40.dp).background(LightMint, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                            Icon(icon, label, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(label, color = TextPrimary, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueChart() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("Biểu đồ doanh thu", color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Đơn vị: Triệu VNĐ", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().height(155.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                revenue.forEachIndexed { index, point ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Text(point.amount, color = if (index == revenue.lastIndex) White else TextSecondary, fontSize = 10.sp,
                            modifier = if (index == revenue.lastIndex) Modifier.background(PrimaryGreen, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp) else Modifier)
                        Spacer(Modifier.height(5.dp))
                        Box(Modifier.width(27.dp).height(point.height.dp).background(if (index == revenue.lastIndex) PrimaryGreen else PrimaryGreen.copy(alpha = .78f), RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)))
                        Spacer(Modifier.height(7.dp))
                        Text(point.month, color = if (index == revenue.lastIndex) PrimaryGreen else TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderItem(reminder: Reminder) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(reminder.room, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                StatusChip(reminder.timeLabel, if (reminder.urgent) AlertBackground else LightMint, if (reminder.urgent) AlertRed else TextSecondary)
            }
            Spacer(Modifier.height(7.dp))
            Text(reminder.detail, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            if (reminder.urgent) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip("Gọi điện", PrimaryGreen, White)
                    StatusChip("Chi tiết", LightMint, PrimaryGreen)
                }
            }
        }
    }
}

@Composable
private fun RentalSummary() {
    Column {
        Text("Khu trọ phụ trách", color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(LightMint, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Dashboard, null, tint = PrimaryGreen)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Nhà trọ Bình An 1", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Dãy A – 10 phòng • 95% lấp đầy", color = TextSecondary, fontSize = 12.sp)
                }
                Icon(Icons.Default.Campaign, null, tint = PrimaryGreen)
            }
        }
    }
}
