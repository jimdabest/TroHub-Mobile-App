package com.uth.trohub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Thẻ Hóa đơn giả lập Skeleton (Loading Placeholder)
 * Sử dụng Modifier.shimmerEffect() để tạo hiệu ứng chuyển động ánh sáng mượt mà.
 */
@Composable
fun SkeletonInvoiceCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hàng đầu: Mã phòng & Trạng thái thanh toán (Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder icon phòng
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shimmerEffect(RoundedCornerShape(10.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        // Tên phòng
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(18.dp)
                                .shimmerEffect(RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.height(6.dp))
                        // Tên người thuê
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .height(14.dp)
                                .shimmerEffect(RoundedCornerShape(4.dp))
                        )
                    }
                }
                // Status pill
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .height(26.dp)
                        .shimmerEffect(RoundedCornerShape(13.dp))
                )
            }

            Spacer(Modifier.height(16.dp))

            // Khối chi tiết dịch vụ (Điện, Nước, Phí dịch vụ)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.width(100.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                        Box(Modifier.width(80.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(Modifier.width(90.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                        Box(Modifier.width(75.dp).height(14.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Hàng chân thẻ: Tổng tiền & Nút QR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Box(Modifier.width(80.dp).height(12.dp).shimmerEffect(RoundedCornerShape(4.dp)))
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.width(120.dp).height(22.dp).shimmerEffect(RoundedCornerShape(6.dp)))
                }
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(36.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

