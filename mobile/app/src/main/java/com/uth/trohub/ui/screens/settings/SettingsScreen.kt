package com.uth.trohub.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.data.model.ServicePriceResponse
import com.uth.trohub.ui.components.PrimaryButton
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightMint)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }

        if (uiState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("Đóng", color = AlertRed)
                    }
                }
            }
        }

        if (uiState.successMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = PrimaryGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("Đóng", color = PrimaryGreen)
                    }
                }
            }
        }

        // --- Mục 0: Thông tin tài khoản người dùng ---
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).background(LightMint, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = uiState.userProfile?.fullName ?: uiState.userProfile?.username ?: "Người dùng",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "@${uiState.userProfile?.username ?: "..."} • Vai trò: ${uiState.userProfile?.role ?: "USER"}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa thông tin", tint = PrimaryGreen)
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = LightMint)
                Spacer(Modifier.height(8.dp))

                Text("Số điện thoại: ${uiState.userProfile?.phone ?: "Chưa cập nhật"}", fontSize = 13.sp, color = TextSecondary)
                Text("Email: ${uiState.userProfile?.email ?: "Chưa cập nhật"}", fontSize = 13.sp, color = TextSecondary)
            }
        }

        // --- Mục 1: Giá & Dịch vụ của nhà trọ ---
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bảng giá dịch vụ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showAddServiceDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                        Spacer(Modifier.width(4.dp))
                        Text("Thêm", color = PrimaryGreen, fontSize = 12.sp)
                    }
                }

                if (uiState.servicePrices.isEmpty()) {
                    Text(
                        text = "Chưa có biểu giá dịch vụ nào cho nhà trọ này. Nhấn 'Thêm' để tạo.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    uiState.servicePrices.forEach { service ->
                        ServicePriceRow(service, formatter)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // --- Mục 2: Thông tin nhận tiền & VietQR ---
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            var inputBankName by remember(uiState.bankName) { mutableStateOf(uiState.bankName) }
            var inputAccount by remember(uiState.bankAccount) { mutableStateOf(uiState.bankAccount) }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thông tin nhận tiền & VietQR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = inputBankName,
                    onValueChange = { inputBankName = it },
                    label = { Text("Ngân hàng thụ hưởng (VD: VCB, MB, Techcombank)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inputAccount,
                    onValueChange = { inputAccount = it },
                    label = { Text("Số tài khoản ngân hàng") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.userProfile?.fullName ?: "",
                    onValueChange = {},
                    label = { Text("Tên chủ tài khoản (theo hồ sơ cá nhân)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
                Button(
                    onClick = { viewModel.saveBankInfo(inputBankName, inputAccount) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lưu thông tin ngân hàng")
                }
            }
        }

        // --- Mục 3: Bảo mật & Hệ thống ---
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bảo mật & Trạng thái", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Đồng bộ dữ liệu Backend", color = TextPrimary, fontSize = 14.sp)
                    StatusChip("Đang kết nối", LightMint, PrimaryGreen)
                }
            }
        }

        // --- Nút Đăng Xuất ---
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = AlertBackground, contentColor = AlertRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AlertRed)
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.userProfile?.fullName ?: "",
            currentPhone = uiState.userProfile?.phone ?: "",
            currentEmail = uiState.userProfile?.email ?: "",
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { name, phone, email ->
                viewModel.updateProfile(name, phone, email)
                showEditProfileDialog = false
            }
        )
    }

    if (showAddServiceDialog) {
        AddServicePriceDialog(
            onDismiss = { showAddServiceDialog = false },
            onConfirm = { name, type, unit, price ->
                viewModel.addServicePrice(name, type, unit, price)
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
private fun ServicePriceRow(service: ServicePriceResponse, formatter: NumberFormat) {
    val formattedPrice = try {
        formatter.format(service.unitPrice)
    } catch (_: Exception) {
        "${service.unitPrice} đ"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(service.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Text("Đơn vị: ${service.unit ?: "Lần/Tháng"} • Loại: ${service.type}", fontSize = 11.sp, color = TextSecondary)
        }
        Text(formattedPrice, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGreen)
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhone: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, phone: String?, email: String?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var email by remember { mutableStateOf(currentEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh Sửa Thông Tin Cá Nhân", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và tên (*)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), phone.trim(), email.trim())
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun AddServicePriceDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, unit: String?, price: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ELECTRIC") }

    val serviceTypes = listOf("ELECTRIC", "WATER", "FIXED", "PER_PERSON")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Biểu Giá Dịch Vụ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên dịch vụ (*) (VD: Điện sinh hoạt)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Đơn giá (VNĐ) (*)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Đơn vị tính (VD: kWh, m3, tháng)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Loại dịch vụ:", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    serviceTypes.forEach { type ->
                        val isSelected = selectedType == type
                        StatusChip(
                            text = type,
                            backgroundColor = if (isSelected) PrimaryGreen else LightMint,
                            textColor = if (isSelected) White else PrimaryGreen,
                            modifier = Modifier.clickable { selectedType = type }
                        )
                    }
                }
            }
        },
        confirmButton = {
            val priceVal = priceStr.toDoubleOrNull()
            Button(
                onClick = {
                    if (name.isNotBlank() && priceVal != null && priceVal >= 0) {
                        onConfirm(name.trim(), selectedType, unit.trim().ifEmpty { null }, priceVal)
                    }
                },
                enabled = name.isNotBlank() && priceVal != null && priceVal >= 0
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
