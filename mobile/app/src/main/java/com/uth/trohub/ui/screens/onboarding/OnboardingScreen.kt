package com.uth.trohub.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BedroomParent
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.uth.trohub.data.local.TokenManager
import com.uth.trohub.ui.theme.LightMint
import com.uth.trohub.ui.theme.PrimaryGreen
import com.uth.trohub.ui.theme.TextPrimary
import com.uth.trohub.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Model biểu diễn từng trang giới thiệu
 */
data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String
)

/**
 * ViewModel quản lý trạng thái hoàn tất Onboarding (sử dụng TokenManager / SharedPreferences)
 */
class OnboardingViewModel : ViewModel() {
    fun isOnboardingCompleted(): Boolean {
        return TokenManager.isOnboardingCompleted()
    }

    fun completeOnboarding() {
        TokenManager.setOnboardingCompleted(true)
    }
}

/**
 * Màn hình giới thiệu Onboarding Screen với Compose Foundation HorizontalPager
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Quản lý Phòng & Khách thuê",
            description = "Nắm bắt tức thì danh sách phòng trống, người đang thuê và hợp đồng minh bạch, tránh nhầm lẫn thất thoát.",
            icon = Icons.Default.BedroomParent,
            badge = "TỐI ƯU CÔNG VIỆC"
        ),
        OnboardingPageData(
            title = "Ghi chỉ số Điện & Nước",
            description = "Ghi số điện nước từng phòng nhanh chóng hàng tháng, hệ thống tự động tính chi phí theo đúng bảng giá định sẵn.",
            icon = Icons.Default.Bolt,
            badge = "CHÍNH XÁC & CÔNG BẰNG"
        ),
        OnboardingPageData(
            title = "Hóa đơn & Mã VietQR tự động",
            description = "Tự động tạo hóa đơn tiền trọ kèm mã QR ngân hàng thanh toán nhanh, hỗ trợ nhắc phí và theo dõi dòng tiền.",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            badge = "TIỆN LỢI & HIỆN ĐẠI"
        )
    )

    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hàng nút "Bỏ qua" ở góc trên cùng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(
                visible = !isLastPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        onFinished()
                    }
                ) {
                    Text(
                        text = "Bỏ qua",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // HorizontalPager native của Jetpack Compose Foundation
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            OnboardingPageItem(data = pages[pageIndex])
        }

        Spacer(Modifier.height(20.dp))

        // Indicator dấu chấm tròn ở dưới cùng
        PageIndicator(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Nút hành động: "Tiếp tục" hoặc "Bắt đầu sử dụng"
        Button(
            onClick = {
                if (isLastPage) {
                    viewModel.completeOnboarding()
                    onFinished()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isLastPage) "Bắt đầu ngay" else "Tiếp tục",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Giao diện hiển thị chi tiết của một trang Onboarding
 */
@Composable
private fun OnboardingPageItem(
    data: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Khối hình minh họa / Icon trang nhã
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(LightMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Badge danh mục
        Box(
            modifier = Modifier
                .background(LightMint.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = data.badge,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // Tiêu đề
        Text(
            text = data.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Mô tả chi tiết
        Text(
            text = data.description,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

/**
 * Indicator hiển thị trang hiện tại (chuyển đổi kích thước linh hoạt dạng con nhộng)
 */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val animatedWidth by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                label = "IndicatorWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(animatedWidth)
                    .background(
                        color = if (isSelected) PrimaryGreen else PrimaryGreen.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

