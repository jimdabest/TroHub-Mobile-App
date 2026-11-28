package com.uth.trohub.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.data.model.ContractResponse
import com.uth.trohub.ui.components.AppTopBar
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.BackgroundGray
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun RevenueReportScreen(
    viewModel: RevenueReportViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onOpenInvoices: () -> Unit = {},
    onExportReport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val now = LocalDate.now()
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val formattedMonthly = try {
        formatter.format(uiState.totalMonthlyRevenue)
    } catch (_: Exception) {
        "${uiState.totalMonthlyRevenue} đ"
    }

    val formattedDeposit = try {
        formatter.format(uiState.totalDeposit)
    } catch (_: Exception) {
        "${uiState.totalDeposit} đ"
    }

    Scaffold(
        topBar = { AppTopBar(title = "Báo Cáo Doanh Thu", onBackClick = onBackClick) },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            if (uiState.errorMessage != null) {
                Card(colors = CardDefaults.cardColors(containerColor = AlertBackground), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(uiState.errorMessage ?: "", color = AlertRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.loadData() }) {
                            Text("Thử lại", color = AlertRed)
                        }
                    }
                }
            }

            PeriodCard(propertyName = uiState.propertyName, currentMonth = now.monthValue, currentYear = now.year)
            RevenueOverview(
                totalMonthly = formattedMonthly,
                totalDeposit = formattedDeposit
            )
            RevenueIndicators(
                occupancyRate = uiState.occupancyRate,
                occupied = uiState.occupiedRooms,
                total = uiState.totalRooms,
                activeCount = uiState.activeContracts.size
            )

            if (uiState.monthlyData.isNotEmpty()) {
                MonthlyBreakdownSection(
                    monthlyData = uiState.monthlyData,
                    formatter = formatter
                )
            }

            RecentContractsSection(
                contracts = uiState.activeContracts,
                formatter = formatter,
                onOpenInvoices = onOpenInvoices
            )
            ExportReportButton(onExportReport)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PeriodCard(propertyName: String, currentMonth: Int, currentYear: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(LightMint, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text("KHU TRỌ: ${propertyName.uppercase()}", color = TextSecondary, fontSize = 10.sp)
                    Text("Kỳ thu: Tháng $currentMonth/$currentYear", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun RevenueOverview(totalMonthly: String, totalDeposit: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OverviewCard("DỰ KIẾN THÁNG", totalMonthly, "Từ các hợp đồng hiệu lực", false, Modifier.weight(1f))
        OverviewCard("TIỀN CỌC ĐANG GIỮ", totalDeposit, "Bảo đảm hợp đồng", false, Modifier.weight(1f))
    }
}

@Composable
private fun OverviewCard(title: String, value: String, subtitle: String, isAlert: Boolean, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = if (isAlert) AlertRed else TextSecondary, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Box(Modifier.size(8.dp).background(if (isAlert) AlertRed else PrimaryGreen, RoundedCornerShape(4.dp)))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, color = if (isAlert) AlertRed else PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun RevenueIndicators(occupancyRate: Int, occupied: Int, total: Int, activeCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Indicator("Tỉ lệ lấp đầy", "$occupancyRate% ($occupied/$total phòng)", Modifier.weight(1f))
        Indicator("Hợp đồng hoạt động", "$activeCount khách đang thuê", Modifier.weight(1f))
    }
}

@Composable
private fun Indicator(title: String, value: String, modifier: Modifier) {
    Column(modifier.background(LightMint, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(title, color = TextSecondary, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun RecentContractsSection(
    contracts: List<ContractResponse>,
    formatter: NumberFormat,
    onOpenInvoices: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hợp đồng thu tiền gần nhất", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                text = "Xem tất cả",
                color = PrimaryGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onOpenInvoices)
            )
        }

        if (contracts.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Chưa có hợp đồng nào đang hiệu lực.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            }
        } else {
            contracts.forEach { contract ->
                val rentFormatted = try { formatter.format(contract.monthlyRent ?: 0.0) } catch (_: Exception) { "${contract.monthlyRent} đ" }
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenInvoices)
                ) {
                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).background(LightMint, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Phòng ${contract.roomCode ?: contract.roomId.take(6)} - ${contract.tenantName ?: "Khách"}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Ký từ: ${contract.startDate}", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text(rentFormatted, color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = TextSecondary, modifier = Modifier.padding(start = 5.dp).size(16.dp))
                    }
                }
            }
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

@Composable
private fun MonthlyBreakdownSection(
    monthlyData: List<com.uth.trohub.data.model.MonthlyRevenueSummary>,
    formatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Lịch sử doanh thu các tháng",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            monthlyData.forEach { month ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            month.period,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            "${month.invoiceCount} hóa đơn",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            formatter.format(month.totalRevenue),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryGreen
                        )
                        Text(
                            "Đã thu: ${formatter.format(month.totalPaid)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                HorizontalDivider(color = LightMint.copy(alpha = 0.5f))
            }
        }
    }
}
