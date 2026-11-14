package com.uth.trohub.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.uth.trohub.navigation.BottomNavItem
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextSecondary
import com.uth.trohub.ui.theme.White

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Room,
        BottomNavItem.Tenant,
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
                label = { Text(text = item.title, fontSize = 11.sp, maxLines = 1) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = LightMint,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        if (item.route == BottomNavItem.Home.route) {
                            // Quay về Trang Chủ: Pop hết các màn hình con để về lại Trang Chủ
                            val popped = navController.popBackStack(BottomNavItem.Home.route, false)
                            if (!popped) {
                                navController.navigate(BottomNavItem.Home.route) {
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(BottomNavItem.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
    }
}
