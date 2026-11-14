package com.uth.trohub.ui.screens.invoices

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình Chi tiết Hóa đơn & Thu tiền (Bước 6)
 * - Hiển thị bảng kê chi tiết: Tiền phòng, điện, nước, dịch vụ
 * - Thông tin tài khoản ngân hàng & Mã VietQR tự động
 * - Nút "Xác nhận đã thu" (Confirm Payment)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoice: InvoiceItemUiModel,
    bankName: String,
    bankAccount: String,
    accountOwner: String,
    onBackClick: () -> Unit,
    onConfirmPayment: (String) -> Unit
) {
    val context = LocalContext.current
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Tạo link ảnh VietQR động theo chuẩn VietQR.io
    val cleanBank = bankName.trim().ifEmpty { "MB" }
    val cleanAcc = bankAccount.trim().ifEmpty { "0838337115" }
    val transferContent = "Phong ${invoice.roomCode} ${invoice.period.replace(" ", "")}"
    val vietQrUrl = "https://img.vietqr.io/image/$cleanBank-$cleanAcc-compact2.png" +
            "?amount=${invoice.totalAmount.toLong()}" +
            "&addInfo=${java.net.URLEncoder.encode(transferContent, "UTF-8")}" +
            "&accountName=${java.net.URLEncoder.encode(accountOwner, "UTF-8")}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chi Tiết Hóa Đơn - P.${invoice.roomCode}",
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
            // 1. Thẻ Tổng tiền & Trạng thái thanh toán
            TotalAmountCard(
                invoice = invoice,
                formatter = formatter
            )

            // 2. Bảng kê chi tiết các khoản phí
            FeeBreakdownCard(
                invoice = invoice,
                formatter = formatter
            )

            // 3. Khung thanh toán VietQR & Thông tin ngân hàng
            VietQrPaymentCard(
                vietQrUrl = vietQrUrl,
                bankName = cleanBank,
                bankAccount = cleanAcc,
                accountOwner = accountOwner,
                amountFormatted = formatter.format(invoice.totalAmount),
                transferContent = transferContent,
                onCopyAccount = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Số tài khoản", cleanAcc))
                    Toast.makeText(context, "Đã sao chép số tài khoản!", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(Modifier.height(8.dp))

            // 4. Nút hành động: "Xác nhận đã thu" (Confirm Payment)
            if (!invoice.isPaid) {
                Button(
                    onClick = {
                        onConfirmPayment(invoice.id)
                        Toast.makeText(context, "Đã xác nhận thu tiền phòng ${invoice.roomCode} thành công!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Xác Nhận Đã Thu Tiền",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Hóa đơn này đã hoàn tất thanh toán",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Thẻ hiển thị Tổng số tiền và trạng thái
 */
@Composable
private fun TotalAmountCard(
    invoice: InvoiceItemUiModel,
    formatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phòng ${invoice.roomCode} • ${invoice.tenantName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Kỳ thanh toán: ${invoice.period}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                StatusChip(
                    text = if (invoice.isPaid) "✓ Đã thu" else "Chờ thanh toán",
                    backgroundColor = if (invoice.isPaid) LightMint else AlertBackground,
                    textColor = if (invoice.isPaid) PrimaryGreen else AlertRed
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = LightMint)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "TỔNG THANH TOÁN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = try { formatter.format(invoice.totalAmount) } catch (_: Exception) { "${invoice.totalAmount} đ" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

/**
 * Bảng kê chi tiết từng khoản phí
 */
@Composable
private fun FeeBreakdownCard(
    invoice: InvoiceItemUiModel,
    formatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Bảng Kê Chi Tiết Khoản Phí",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(14.dp))

            // 1. Tiền phòng
            FeeLineItem(
                title = "1. Tiền thuê phòng cơ bản",
                subTitle = "Cố định theo hợp đồng",
                amountFormatted = formatter.format(invoice.roomRent)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LightMint.copy(alpha = 0.6f))

            // 2. Tiền điện
            FeeLineItem(
                title = "2. Tiền điện sinh hoạt",
                subTitle = "Số cũ: ${invoice.electricOld.toInt()} | Số mới: ${invoice.electricNew.toInt()} (Dùng: ${invoice.electricUsage.toInt()} kWh x ${formatter.format(invoice.electricUnitPrice)})",
                amountFormatted = formatter.format(invoice.electricAmount)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LightMint.copy(alpha = 0.6f))

            // 3. Tiền nước
            FeeLineItem(
                title = "3. Tiền nước sinh hoạt",
                subTitle = "Số cũ: ${invoice.waterOld.toInt()} | Số mới: ${invoice.waterNew.toInt()} (Dùng: ${invoice.waterUsage.toInt()} m³ x ${formatter.format(invoice.waterUnitPrice)})",
                amountFormatted = formatter.format(invoice.waterAmount)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LightMint.copy(alpha = 0.6f))

            // 4. Dịch vụ khác
            FeeLineItem(
                title = "4. Phí dịch vụ chung",
                subTitle = "Rác, Wifi, Vệ sinh khu vực chung",
                amountFormatted = formatter.format(invoice.otherServicesAmount)
            )
        }
    }
}

@Composable
private fun FeeLineItem(
    title: String,
    subTitle: String,
    amountFormatted: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subTitle, fontSize = 11.sp, color = TextSecondary)
        }
        Text(amountFormatted, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
    }
}

/**
 * Khung hiển thị VietQR và thông tin ngân hàng thụ hưởng
 */
@Composable
private fun VietQrPaymentCard(
    vietQrUrl: String,
    bankName: String,
    bankAccount: String,
    accountOwner: String,
    amountFormatted: String,
    transferContent: String,
    onCopyAccount: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.QrCode2, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Quét Mã VietQR Chuyển Khoản",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(14.dp))

            // Hình ảnh mã VietQR tự động sinh từ coil
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightMint.copy(alpha = 0.5f))
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = vietQrUrl,
                    contentDescription = "Mã VietQR thanh toán",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(14.dp))

            // Thông tin tài khoản dạng danh mục
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightMint.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ngân hàng:", fontSize = 12.sp, color = TextSecondary)
                    Text(bankName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Số tài khoản:", fontSize = 12.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(bankAccount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = onCopyAccount, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryGreen, modifier = Modifier.size(15.dp))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Chủ tài khoản:", fontSize = 12.sp, color = TextSecondary)
                    Text(accountOwner, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Số tiền:", fontSize = 12.sp, color = TextSecondary)
                    Text(amountFormatted, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryGreen)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nội dung CK:", fontSize = 12.sp, color = TextSecondary)
                    Text(transferContent, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
    }
}
