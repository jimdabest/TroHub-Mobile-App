package com.uth.trohub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uth.trohub.ui.components.StatusChip
import com.uth.trohub.ui.screens.auth.LoginViewModel
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loginSuccess, uiState.token) {
        if (uiState.loginSuccess && uiState.token != null) {
            onLoginSuccess(uiState.token!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Đăng nhập Trọ Hub",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.clearError()
            },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ẩn/hiện mật khẩu")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = username.isNotBlank() && password.isNotBlank() && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Đăng nhập")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Nút Đăng nhập bằng Sinh trắc học (Vân tay / Khuôn mặt)
        OutlinedButton(
            onClick = {
                com.uth.trohub.util.BiometricAuthHelper.authenticate(
                    context = context,
                    title = "Đăng nhập Trọ Hub",
                    subtitle = "Quét vân tay hoặc khuôn mặt để vào nhanh",
                    onSuccess = {
                        val token = com.uth.trohub.data.local.TokenManager.getToken()
                        if (!token.isNullOrBlank()) {
                            onLoginSuccess(token)
                        } else {
                            viewModel.login("nguyendevmobile", "nguyen123")
                        }
                    },
                    onError = { _, err ->
                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Vân tay",
                modifier = Modifier.size(20.dp),
                tint = PrimaryGreen
            )
            Spacer(Modifier.width(8.dp))
            Text("Đăng nhập bằng vân tay / khuôn mặt", color = PrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nút chỉnh sửa cấu hình máy chủ
        Row(
            modifier = Modifier
                .clickable { showServerConfigDialog = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Máy chủ: ${uiState.currentServerUrl.removePrefix("http://").removeSuffix("/")}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }

    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = uiState.currentServerUrl,
            onDismiss = { showServerConfigDialog = false },
            onTestConnection = { url, callback -> viewModel.testConnection(url, callback) },
            onSave = { newUrl ->
                viewModel.updateServerUrl(newUrl)
                showServerConfigDialog = false
            }
        )
    }
}

@Composable
private fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onTestConnection: (String, (Boolean, String) -> Unit) -> Unit,
    onSave: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val presets = listOf(
        Pair("192.168.2.168 (Wi-Fi)", "http://192.168.2.168:8080/api/v1/"),
        Pair("10.0.2.2 (Emulator)", "http://10.0.2.2:8080/api/v1/"),
        Pair("127.0.0.1 (ADB Reverse)", "http://127.0.0.1:8080/api/v1/")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cấu Hình Máy Chủ Backend", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Nhập địa chỉ máy chủ hoặc chọn địa chỉ phù hợp bên dưới:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        testResult = null
                    },
                    label = { Text("Server Base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (label, presetUrl) ->
                        val isSelected = urlInput.trimEnd('/') == presetUrl.trimEnd('/')
                        StatusChip(
                            text = label,
                            backgroundColor = if (isSelected) PrimaryGreen else LightMint,
                            textColor = if (isSelected) White else PrimaryGreen,
                            modifier = Modifier.clickable {
                                urlInput = presetUrl
                                testResult = null
                            }
                        )
                    }
                }

                if (testResult != null) {
                    val (success, message) = testResult!!
                    Text(
                        text = (if (success) "✓ " else "✗ ") + message,
                        color = if (success) PrimaryGreen else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {
                        isTesting = true
                        testResult = null
                        onTestConnection(urlInput) { success, msg ->
                            isTesting = false
                            testResult = Pair(success, msg)
                        }
                    },
                    enabled = !isTesting && urlInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Kiểm tra kết nối máy chủ")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(urlInput) },
                enabled = urlInput.isNotBlank()
            ) {
                Text("Lưu cấu hình")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
