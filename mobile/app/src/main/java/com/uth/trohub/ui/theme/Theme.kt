package com.uth.trohub.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = LightMint,
    background = BackgroundGray,
    surface = White,
    error = AlertRed,
    onPrimary = White,
    onSecondary = PrimaryGreen,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = White
)

@Composable
fun TroHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Tạm thời bỏ qua darkTheme để bám sát Figma
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Đổi màu thanh trạng thái (cục pin, giờ) trên cùng thành màu xanh lá đậm
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Lấy từ file Type.kt mặc định
        content = content
    )
}