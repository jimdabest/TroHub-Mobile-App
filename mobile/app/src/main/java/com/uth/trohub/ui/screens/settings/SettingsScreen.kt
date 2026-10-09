package com.uth.trohub.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uth.trohub.ui.components.PrimaryButton
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary

@Composable
fun SettingsScreen() {
    // Dùng Column kết hợp verticalScroll để màn hình có thể cuộn lên xuống được
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // --- Mục 1: Giá & Dịch vụ mặc định ---
        Text("Giá & Dịch vụ mặc định", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        OutlinedTextField(
            value = "3.500",
            onValueChange = { /* Xử lý đổi text sau */ },
            label = { Text("Giá điện sinh hoạt (đ/kWh)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "25.000",
            onValueChange = {},
            label = { Text("Giá nước sinh hoạt (đ/m3)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "100.000",
            onValueChange = {},
            label = { Text("Phụ thu chung (Rác, Wifi, Vệ sinh)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Mục 2: Thông tin nhận tiền & VietQR ---
        Text("Thông tin nhận tiền & VietQR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        OutlinedTextField(
            value = "Vietcombank (VCB)",
            onValueChange = {},
            label = { Text("Ngân hàng thụ hưởng") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "9838337115",
            onValueChange = {},
            label = { Text("Số tài khoản") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "PHAM NGUYEN", // Đặt tên bạn hoặc tên chủ trọ
            onValueChange = {},
            label = { Text("Tên chủ tài khoản") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Mục 3: Bảo mật ---
        Text("Bảo mật", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Khóa bằng Vân tay / PIN")
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = com.uth.trohub.ui.theme.LightMint)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Đồng bộ dữ liệu Zalo / Cloud")
            Switch(
                checked = true,
                onCheckedChange = {},
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = com.uth.trohub.ui.theme.LightMint)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Nút Lưu ---
        PrimaryButton(
            text = "Lưu thay đổi",
            onClick = { /* Xử lý lưu sau */ }
        )

        // Tạo khoảng trống dưới cùng để không bị thanh BottomBar che khuất
        Spacer(modifier = Modifier.height(32.dp))
    }
}