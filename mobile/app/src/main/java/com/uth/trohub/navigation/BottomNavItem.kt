package com.uth.trohub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Trang chủ")
    object Room : BottomNavItem("room", Icons.Default.BedroomParent, "Phòng")
    object Tenant : BottomNavItem("tenant", Icons.Default.People, "Khách thuê")
    object Invoice : BottomNavItem("invoice", Icons.Default.ReceiptLong, "Hóa đơn")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Cài đặt")
}
