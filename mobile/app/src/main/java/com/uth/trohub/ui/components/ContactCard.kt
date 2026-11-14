package com.uth.trohub.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary

/**
 * Thẻ Liên hệ nhanh (ContactCard):
 * - Hỗ trợ mở bàn phím quay số với ACTION_DIAL (không yêu cầu nguy hiểm permission CALL_PHONE).
 * - Hỗ trợ mở chat Zalo trực tiếp qua URL Scheme https://zalo.me/{số_điện_thoại}.
 */
@Composable
fun ContactCard(
    tenantName: String,
    phoneNumber: String,
    roomCode: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Thông tin cơ bản người thuê
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(LightMint, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = tenantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Phòng $roomCode • SĐT: $phoneNumber",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 2 Nút hành động nhanh: Gọi điện & Zalo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nút 1: Gọi điện (Intent.ACTION_DIAL - An toàn, không cần quyền CALL_PHONE)
                Button(
                    onClick = { dialPhoneNumber(context, phoneNumber) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Gọi điện",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(text = "Gọi điện", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Nút 2: Nhắn Zalo (Intent.ACTION_VIEW với link https://zalo.me/)
                OutlinedButton(
                    onClick = { openZaloChat(context, phoneNumber) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF0068FF) // Zalo blue branding
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Nhắn Zalo",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0068FF)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Nhắn Zalo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0068FF)
                    )
                }
            }
        }
    }
}

/**
 * Mở màn hình quay số điện thoại của hệ điều hành.
 * Sử dụng Intent.ACTION_DIAL thay vì Intent.ACTION_CALL để:
 * 1. KHÔNG vi phạm chính sách cấp quyền nguy hiểm của Google Play Store.
 * 2. Cho phép người dùng xác nhận và chủ động bấm nút gọi.
 */
private fun dialPhoneNumber(context: Context, rawPhone: String) {
    try {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Không thể mở ứng dụng gọi điện: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Mở cuộc trò chuyện Zalo với người thuê qua Deep link https://zalo.me/{số_điện_thoại}
 * - Tự động chuẩn hóa số điện thoại Việt Nam (ví dụ 098... -> 8498... hoặc giữ nguyên định dạng hợp lệ).
 * - Nếu Zalo đã cài trên máy, Android sẽ ưu tiên mở app Zalo; nếu không có thể mở trình duyệt web.
 * - Có xử lý try-catch chống crash và thông báo Toast thân thiện.
 */
private fun openZaloChat(context: Context, rawPhone: String) {
    try {
        // Lọc bỏ ký tự thừa: khoảng trắng, dấu gạch ngang, ngoặc đơn
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            // Định dạng quốc tế chuẩn của Zalo: 84xxx
            cleanPhone = "84" + cleanPhone.substring(1)
        }

        val zaloUri = Uri.parse("https://zalo.me/$cleanPhone")
        val zaloIntent = Intent(Intent.ACTION_VIEW, zaloUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(zaloIntent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Không thể mở Zalo. Vui lòng kiểm tra ứng dụng Zalo trên máy của bạn.",
            Toast.LENGTH_LONG
        ).show()
    }
}
