package me.ihqqq.trohub.util;

import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;

public final class PaymentResultPage {
    private PaymentResultPage() {}

    public static String render(boolean validSignature, boolean success, boolean cancelled,
                                String errorCode, String txnRef, BigDecimal amount) {
        String title;
        String detail;
        String color;
        if (!validSignature) {
            title = "Không xác thực được kết quả";
            detail = "Dữ liệu trả về không hợp lệ. Vui lòng kiểm tra lại trạng thái hóa đơn trong ứng dụng.";
            color = "#b3261e";
        } else if (success) {
            title = "Thanh toán thành công";
            detail = "Hệ thống đang cập nhật hóa đơn. Bạn có thể quay lại ứng dụng.";
            color = "#1b7f3b";
        } else if (cancelled) {
            title = "Bạn đã hủy giao dịch";
            detail = "Hóa đơn chưa được thanh toán. Bạn có thể quay lại ứng dụng để thử lại.";
            color = "#8a6d00";
        } else {
            title = "Thanh toán không thành công";
            detail = "Mã lỗi: " + HtmlUtils.htmlEscape(String.valueOf(errorCode))
                    + ". Bạn có thể quay lại ứng dụng để thử lại.";
            color = "#b3261e";
        }
        String extra = validSignature
                ? "<p class=\"meta\">Mã giao dịch: " + HtmlUtils.htmlEscape(String.valueOf(txnRef))
                + (amount != null ? "<br>Số tiền: " + amount.toBigInteger() + " đ" : "") + "</p>"
                : "";
        return """
                <!DOCTYPE html>
                <html lang="vi"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Kết quả thanh toán</title>
                <style>
                body{font-family:system-ui,sans-serif;display:flex;min-height:100vh;margin:0;align-items:center;justify-content:center;background:#f5f5f5}
                .card{background:#fff;max-width:360px;margin:16px;padding:28px;border-radius:16px;box-shadow:0 2px 12px #0001;text-align:center}
                h1{font-size:20px;margin:0 0 12px;color:%s}
                p{color:#444;line-height:1.5;margin:8px 0}
                .meta{font-size:13px;color:#777}
                </style></head><body><div class="card"><h1>%s</h1><p>%s</p>%s</div></body></html>
                """.formatted(color, title, detail, extra);
    }
}