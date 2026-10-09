package com.uth.trohub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Trang chủ")
    object Room : BottomNavItem("room", Icons.Default.List, "Phòng")
    object Invoice : BottomNavItem("invoice", Icons.Default.Menu, "Hóa đơn")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Cài đặt")
}