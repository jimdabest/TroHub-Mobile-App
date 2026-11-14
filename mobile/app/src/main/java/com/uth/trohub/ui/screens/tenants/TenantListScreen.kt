package com.uth.trohub.ui.screens.tenants

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.theme.AlertBackground
import com.uth.trohub.ui.theme.AlertRed
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

@Composable
fun TenantListScreen(
    viewModel: TenantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTenant by remember { mutableStateOf<TenantItemUiModel?>(null) }
    var deletingTenant by remember { mutableStateOf<TenantItemUiModel?>(null) }

    val filteredTenants = uiState.tenants.filter { tenant ->
        val query = uiState.searchQuery.trim().lowercase()
        if (query.isBlank()) true
        else {
            tenant.fullName.lowercase().contains(query) ||
                    (tenant.phone?.contains(query) == true) ||
                    (tenant.idNumber?.contains(query) == true) ||
                    (tenant.rentingRoomCode?.lowercase()?.contains(query) == true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightMint),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = uiState.propertyName.ifEmpty { "Khu nhà trọ" },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Quản lý khách thuê",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm khách", fontSize = 12.sp)
                }
            }
        }

        // Ô tìm kiếm
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Tìm theo tên, SĐT, CCCD, số phòng...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                singleLine = true
            )
        }

        // Thông báo lỗi hoặc thành công
        if (uiState.errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("Đóng", fontSize = 12.sp, color = AlertRed)
                        }
                    }
                }
            }
        }

        if (uiState.successMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightMint),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.successMessage ?: "",
                            color = PrimaryGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("Đóng", fontSize = 12.sp, color = PrimaryGreen)
                        }
                    }
                }
            }
        }

        // Danh sách khách thuê
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
        } else if (filteredTenants.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.tenants.isEmpty())
                                "Chưa có khách thuê nào trong khu trọ này.\nBấm 'Thêm khách' để bắt đầu lưu trữ!"
                            else
                                "Không tìm thấy khách thuê phù hợp với tìm kiếm.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTenants) { tenant ->
                TenantCard(
                    tenant = tenant,
                    onCall = {
                        tenant.phone?.let { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    },
                    onSms = {
                        tenant.phone?.let { phone ->
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
                            context.startActivity(intent)
                        }
                    },
                    onEdit = { editingTenant = tenant },
                    onDelete = { deletingTenant = tenant }
                )
            }
        }
    }

    if (showAddDialog) {
        TenantFormDialog(
            title = "Thêm Khách Thuê Mới",
            initialName = "",
            initialPhone = "",
            initialIdNumber = "",
            initialNote = "",
            confirmButtonText = "Thêm",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, idNumber, note ->
                viewModel.createTenant(name, phone, idNumber, note) {
                    showAddDialog = false
                }
            }
        )
    }

    if (editingTenant != null) {
        TenantFormDialog(
            title = "Chỉnh Sửa Thông Tin Khách",
            initialName = editingTenant!!.fullName,
            initialPhone = editingTenant!!.phone ?: "",
            initialIdNumber = editingTenant!!.idNumber ?: "",
            initialNote = editingTenant!!.note ?: "",
            confirmButtonText = "Lưu thay đổi",
            onDismiss = { editingTenant = null },
            onConfirm = { name, phone, idNumber, note ->
                viewModel.updateTenant(editingTenant!!.id, name, phone, idNumber, note) {
                    editingTenant = null
                }
            }
        )
    }

    if (deletingTenant != null) {
        AlertDialog(
            onDismissRequest = { deletingTenant = null },
            title = { Text("Xác nhận xóa khách thuê", fontWeight = FontWeight.Bold) },
            text = {
                Text("Bạn có chắc chắn muốn xóa khách thuê ${deletingTenant!!.fullName}? " +
                        "Lưu ý: Chỉ có thể xóa khi khách không còn hợp đồng thuê hiệu lực.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = deletingTenant!!.id
                        deletingTenant = null
                        viewModel.deleteTenant(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTenant = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun TenantCard(
    tenant: TenantItemUiModel,
    onCall: () -> Unit,
    onSms: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(LightMint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = tenant.fullName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    if (!tenant.rentingRoomCode.isNullOrBlank()) {
                        StatusChip(
                            text = "Đang thuê P.${tenant.rentingRoomCode}",
                            backgroundColor = LightMint,
                            textColor = PrimaryGreen
                        )
                    } else {
                        Text(
                            text = "Chưa có phòng thuê",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = AlertRed, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = LightMint.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "SĐT: ${tenant.phone?.ifEmpty { "Chưa cập nhật" } ?: "Chưa cập nhật"}",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "CCCD: ${tenant.idNumber?.ifEmpty { "Chưa có CCCD" } ?: "Chưa có CCCD"}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (!tenant.note.isNullOrBlank()) {
                        Text(
                            text = "Ghi chú: ${tenant.note}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (!tenant.phone.isNullOrBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier.size(34.dp).background(LightMint, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Gọi", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onSms,
                            modifier = Modifier.size(34.dp).background(LightMint, CircleShape)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Nhắn tin", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantFormDialog(
    title: String,
    initialName: String,
    initialPhone: String,
    initialIdNumber: String,
    initialNote: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, phone: String?, idNumber: String?, note: String?) -> Unit
) {
    var fullName by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var idNumber by remember { mutableStateOf(initialIdNumber) }
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên (*)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { idNumber = it },
                    label = { Text("CCCD / CMND") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (quê quán, xe...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank()) {
                        onConfirm(
                            fullName.trim(),
                            phone.trim().ifEmpty { null },
                            idNumber.trim().ifEmpty { null },
                            note.trim().ifEmpty { null }
                        )
                    }
                },
                enabled = fullName.isNotBlank()
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
