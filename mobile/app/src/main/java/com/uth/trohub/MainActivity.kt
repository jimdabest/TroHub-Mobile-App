package com.uth.trohub

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.navigation.AppRoute
import com.uth.trohub.navigation.BottomNavItem
import com.uth.trohub.ui.components.AppBottomBar
import com.uth.trohub.ui.components.AppTopBar
import com.uth.trohub.ui.screens.LoginScreen
import com.uth.trohub.ui.screens.auth.RegisterScreen
import com.uth.trohub.ui.screens.home.HomeScreen
import com.uth.trohub.ui.screens.invoices.InvoiceListScreen
import com.uth.trohub.ui.screens.meters.MeterReadingScreen
import com.uth.trohub.ui.screens.reports.RevenueReportScreen
import com.uth.trohub.ui.screens.rooms.RoomListScreen
import com.uth.trohub.ui.screens.settings.SettingsScreen
import com.uth.trohub.ui.screens.tenants.TenantListScreen
import com.uth.trohub.ui.theme.TroHubTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(this)
        enableEdgeToEdge()

        setContent {
            TroHubTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // Lấy route hiện tại để đổi tiêu đề TopBar tương ứng
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val topBarTitle = when (currentRoute) {
                    BottomNavItem.Home.route -> "Trang Chủ"
                    BottomNavItem.Room.route -> "Danh Sách Phòng"
                    BottomNavItem.Tenant.route -> "Khách Thuê"
                    BottomNavItem.Invoice.route -> "Thu Tiền & Hóa Đơn"
                    BottomNavItem.Settings.route -> "Cài Đặt"
                    else -> "Trọ Hub"
                }
                val showMainNavigation = currentRoute in setOf(
                    BottomNavItem.Home.route,
                    BottomNavItem.Room.route,
                    BottomNavItem.Tenant.route,
                    BottomNavItem.Invoice.route,
                    BottomNavItem.Settings.route
                )

                Scaffold(
                    topBar = {
                        if (showMainNavigation) AppTopBar(title = topBarTitle)
                    },
                    bottomBar = {
                        if (showMainNavigation) AppBottomBar(navController = navController)
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val initialDestination = if (TokenManager.isLoggedIn()) {
                            BottomNavItem.Home.route
                        } else if (!TokenManager.isOnboardingCompleted()) {
                            AppRoute.Onboarding
                        } else {
                            AppRoute.Login
                        }

                        // Khai báo các màn hình
                        NavHost(
                            navController = navController,
                            startDestination = initialDestination
                        ) {
                            composable(AppRoute.Onboarding) {
                                com.uth.trohub.ui.screens.onboarding.OnboardingScreen(
                                    onFinished = {
                                        navController.navigate(AppRoute.Login) {
                                            popUpTo(AppRoute.Onboarding) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoute.Login) {
                                LoginScreen(
                                    onLoginSuccess = { token ->
                                        TokenManager.saveToken(token)
                                        navController.navigate(BottomNavItem.Home.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    },
                                    onNavigateToRegister = {
                                        navController.navigate(AppRoute.Register)
                                    }
                                )
                            }
                            composable(AppRoute.Register) {
                                RegisterScreen(
                                    onRegisterSuccess = {
                                        navController.popBackStack()
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(BottomNavItem.Home.route) {
                                HomeScreen(
                                    onOpenRooms = { navController.navigate(BottomNavItem.Room.route) },
                                    onOpenTenants = { navController.navigate(BottomNavItem.Tenant.route) },
                                    onOpenMeterReading = { navController.navigate(AppRoute.MeterReading) },
                                    onOpenRevenueReport = { navController.navigate(AppRoute.RevenueReport) },
                                    onOpenInvoices = { navController.navigate(BottomNavItem.Invoice.route) }
                                )
                            }
                            composable(BottomNavItem.Room.route) {
                                RoomListScreen(
                                    onOpenMeterReading = { navController.navigate(AppRoute.MeterReading) },
                                    onOpenInvoices = { navController.navigate(BottomNavItem.Invoice.route) }
                                )
                            }
                            composable(BottomNavItem.Tenant.route) {
                                TenantListScreen()
                            }
                            composable(BottomNavItem.Invoice.route) {
                                InvoiceListScreen()
                            }
                            composable(BottomNavItem.Settings.route) {
                                SettingsScreen(
                                    onLogout = {
                                        navController.navigate(AppRoute.Login) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoute.MeterReading) {
                                MeterReadingScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onSaveAndNext = { navController.popBackStack() },
                                    onSkip = { navController.popBackStack() }
                                )
                            }
                            composable(AppRoute.RevenueReport) {
                                RevenueReportScreen(
                                    onBackClick = { navController.popBackStack() },
                                    onOpenInvoices = { navController.navigate(BottomNavItem.Invoice.route) },
                                    onExportReport = {
                                        Toast.makeText(context, "Đang chuẩn bị báo cáo", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
