package com.uth.trohub.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.components.AppTopBar
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.BackgroundGray
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

private data class RevenueBar(val month: String, val amount: String, val barHeight: Int)

private data class RecentInvoice(val title: String, val dueDate: String, val amount: String)

private val revenueBars = listOf(
    RevenueBar("Th 5", "19.5", 120), RevenueBar("Th 6", "20.2", 132),
    RevenueBar("Th 7", "21.0", 140), RevenueBar("Th 8", "20.8", 137),
    RevenueBar("Th 9", "21.5", 145), RevenueBar("Th 10", "22.0", 150)
)

private val recentInvoices = listOf(
    RecentInvoice("Hóa đơn tháng 10", "Hạn chốt: 05/11/2026", "3.200.000 đ"),
    RecentInvoice("Hóa đơn tháng 09", "Đã thanh toán: 03/10/2026", "3.450.000 đ")
)

@Composable
fun RevenueReportScreen(
    onBackClick: () -> Unit = {},
    onOpenInvoices: () -> Unit = {},
    onExportReport: () -> Unit = {}
) {
    Scaffold(
        topBar = { AppTopBar(title = "Doanh Thu", onBackClick = onBackClick) },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PeriodSelector()
            RevenueOverview()
            RevenueBarChart()
            RevenueIndicators()
            RecentInvoicesSection(onOpenInvoices)
            ExportReportButton(onExportReport)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PeriodSelector() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(LightMint, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("KHOẢNG THỜI GIAN", color = TextSecondary, fontSize = 10.sp)
                    Text("6 tháng gần nhất (T5 – T10/2026)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text("⌄", color = TextSecondary, fontSize = 18.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                PeriodChip("6 tháng", selected = true)
                PeriodChip("Năm nay", selected = false)
                PeriodChip("Tùy chọn", selected = false)
            }
        }
    }
}

@Composable
private fun PeriodChip(text: String, selected: Boolean) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (selected) White else TextSecondary,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.background(if (selected) PrimaryGreen else LightMint, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun RevenueOverview() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OverviewCard("TỔNG ĐÃ THU", "125.000.000 đ", "Trong 6 kỳ thu", false, Modifier.weight(1f))
        OverviewCard("TỔNG CÒN NỢ", "3.200.000 đ", "1 phòng quá hạn", true, Modifier.weight(1f))
    }
}

@Composable
private fun OverviewCard(title: String, value: String, subtitle: String, isDebt: Boolean, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = if (isDebt) AlertRed else TextSecondary, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Box(Modifier.size(8.dp).background(if (isDebt) AlertRed else PrimaryGreen, RoundedCornerShape(4.dp)))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, color = if (isDebt) AlertRed else PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RevenueBarChart() {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Biểu đồ doanh thu", color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Đơn vị: Triệu VNĐ", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(195.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                revenueBars.forEachIndexed { index, item ->
                    RevenueBarItem(item, highlighted = index == revenueBars.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun RevenueBarItem(item: RevenueBar, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Text(
            item.amount,
            color = if (highlighted) White else TextSecondary,
            fontSize = 10.sp,
            modifier = if (highlighted) Modifier.background(PrimaryGreen, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp) else Modifier
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier.width(29.dp).height(item.barHeight.dp).background(
                if (highlighted) PrimaryGreen else PrimaryGreen.copy(alpha = .75f),
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(item.month, color = if (highlighted) PrimaryGreen else TextSecondary, fontSize = 11.sp, fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun RevenueIndicators() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Indicator("Trung bình", "20.8 tr/tháng", Modifier.weight(1f))
        Indicator("Tỉ lệ lấp đầy", "95% (19/20)", Modifier.weight(1f))
    }
}

@Composable
private fun Indicator(title: String, value: String, modifier: Modifier) {
    Column(modifier.background(LightMint, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(title, color = TextSecondary, fontSize = 10.sp)
        Text(value, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun RecentInvoicesSection(onOpenInvoices: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hóa đơn gần nhất", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Tất cả", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        recentInvoices.forEach { invoice -> RecentInvoiceItem(invoice, onOpenInvoices) }
    }
}

@Composable
private fun RecentInvoiceItem(invoice: RecentInvoice, onOpenInvoices: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenInvoices)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(LightMint, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(invoice.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(invoice.dueDate, color = TextSecondary, fontSize = 11.sp)
            }
            Text(invoice.amount, color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextSecondary, modifier = Modifier.padding(start = 5.dp).size(17.dp))
        }
    }
}

@Composable
private fun ExportReportButton(onExportReport: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onExportReport)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Download, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(7.dp))
            Text("Xuất báo cáo (Excel / PDF)", color = PrimaryGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
