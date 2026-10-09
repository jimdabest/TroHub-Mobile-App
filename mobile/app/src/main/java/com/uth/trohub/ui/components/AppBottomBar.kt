package com.uth.trohub.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.uth.trohub.navigation.BottomNavItem
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Room,
        BottomNavItem.Invoice,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = White,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen, // Màu icon khi chọn[cite: 5]
                    selectedTextColor = PrimaryGreen, // Màu chữ khi chọn[cite: 5]
                    indicatorColor = com.uth.trohub.ui.theme.LightMint, // Nền icon khi chọn[cite: 5]
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}