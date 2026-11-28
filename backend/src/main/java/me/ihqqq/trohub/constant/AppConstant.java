package me.ihqqq.trohub.constant;

import java.time.Duration;
import java.time.ZoneId;

public final class AppConstant {
    private AppConstant() {}

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Thời gian một giao dịch online PENDING còn được coi là có thể thanh toán. Lớn hơn hạn của cổng
     * (VNPay 15 phút) để IPN đến trễ vẫn khớp được giao dịch trước khi bị đánh dấu hết hạn.
     */
    public static final Duration PENDING_PAYMENT_TTL = Duration.ofMinutes(30);
}