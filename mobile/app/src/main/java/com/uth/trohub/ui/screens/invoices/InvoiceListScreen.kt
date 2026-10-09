package com.uth.trohub.ui.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.components.PrimaryButton
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

private data class Invoice(
    val room: String,
    val tenant: String,
    val phone: String,
    val total: String,
    val detail: String,
    val paid: Boolean
)

private val invoices = listOf(
    Invoice("Phòng 101", "Nguyễn Văn A", "0912 345 678", "3.200.000 đ", "Tiền phòng", true),
    Invoice("Phòng 102", "Trần Văn Bình", "0988 221 443", "3.200.000 đ", "Tiền phòng + Tiền điện", true),
    Invoice("Phòng 204", "Lê Thị Cúc", "0905 119 772", "2.100.000 đ", "Phí DV & điện nước", true),
    Invoice("Phòng 201", "Lê Hoàng Long", "0936 112 202", "3.500.000 đ", "Chờ thanh toán", false)
)

@Composable
fun InvoiceListScreen() {
    LazyColumn(
        modifier = Modifier.background(LightMint),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { InvoiceFilters() }
        items(invoices) { invoice -> InvoiceItem(invoice) }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun InvoiceFilters() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterButton(Icons.Default.Domain, "Nhà trọ Mai An (18 phòng)", Modifier.weight(1.25f))
        FilterButton(Icons.Default.CalendarMonth, "Tháng 10/2026", Modifier.weight(1f))
    }
}

@Composable
private fun FilterButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = PrimaryGreen),
        border = BorderStroke(1.dp, LightMint)
    ) {
        Icon(icon, null, modifier = Modifier.padding(end = 5.dp))
        Text(label, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun InvoiceItem(invoice: Invoice) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(invoice.room, color = PrimaryGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("${invoice.tenant}  •  ${invoice.phone}", color = TextSecondary, fontSize = 12.sp)
                }
                Text(invoice.total, color = PrimaryGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(invoice.detail, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(15.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(if (invoice.paid) "✓ Đã thanh toán" else "Chờ thanh toán", if (invoice.paid) LightMint else AlertBackground, if (invoice.paid) PrimaryGreen else AlertRed)
                Spacer(Modifier.weight(1f))
                if (invoice.paid) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = White, contentColor = PrimaryGreen),
                        border = BorderStroke(1.dp, LightMint)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.padding(end = 5.dp))
                        Text("Chi tiết", fontSize = 12.sp)
                    }
                }
            }
            if (!invoice.paid) {
                Spacer(Modifier.height(12.dp))
                PrimaryButton(text = "Xác nhận đã thu", onClick = {})
            }
        }
    }
}
