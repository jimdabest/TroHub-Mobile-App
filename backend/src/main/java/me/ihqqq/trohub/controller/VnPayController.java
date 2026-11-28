package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.OnlinePaymentResponse;
import me.ihqqq.trohub.service.VnPayService;
import me.ihqqq.trohub.util.ClientIpUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "VNPay")
public class VnPayController {

    VnPayService vnPayService;

    @PostMapping("/properties/{propertyId}/invoices/{invoiceId}/vnpay/create-url")
    ApiResponse<OnlinePaymentResponse> createUrl(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                                 @RequestBody(required = false) OnlinePaymentRequest request,
                                                 HttpServletRequest http) {
        return ApiResponse.ok(vnPayService.createPaymentUrl(propertyId, invoiceId, request, ClientIpUtils.resolve(http)));
    }

    @GetMapping("/payments/vnpay/ipn")
    Map<String, String> ipn(HttpServletRequest http) {
        Map<String, String> params = new HashMap<>();
        http.getParameterMap().forEach((k, v) -> {
            if (v.length > 0) params.put(k, v[0]);
        });
        try {
            return vnPayService.handleIpn(params);
        } catch (Exception e) {
            log.error("Lỗi xử lý VNPay IPN", e);
            return VnPayService.response("99", "Unknown error");
        }
    }

    @GetMapping(value = "/payments/vnpay/return", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    String returnPage(HttpServletRequest http) {
        Map<String, String> params = new HashMap<>();
        http.getParameterMap().forEach((k, v) -> {
            if (v.length > 0) params.put(k, v[0]);
        });
        var result = vnPayService.verifyReturn(params);

        String title;
        String detail;
        String color;
        if (!result.validSignature()) {
            title = "Không xác thực được kết quả";
            detail = "Dữ liệu trả về không hợp lệ. Vui lòng kiểm tra lại trạng thái hóa đơn trong ứng dụng.";
            color = "#b3261e";
        } else if (result.success()) {
            title = "Thanh toán thành công";
            detail = "Hệ thống đang cập nhật hóa đơn. Bạn có thể quay lại ứng dụng.";
            color = "#1b7f3b";
        } else if ("24".equals(result.responseCode())) {
            title = "Bạn đã hủy giao dịch";
            detail = "Hóa đơn chưa được thanh toán. Bạn có thể quay lại ứng dụng để thử lại.";
            color = "#8a6d00";
        } else {
            title = "Thanh toán không thành công";
            detail = "Mã lỗi VNPay: " + HtmlUtils.htmlEscape(String.valueOf(result.responseCode()))
                    + ". Bạn có thể quay lại ứng dụng để thử lại.";
            color = "#b3261e";
        }
        String extra = result.validSignature()
                ? "<p class=\"meta\">Mã giao dịch: " + HtmlUtils.htmlEscape(String.valueOf(result.txnRef()))
                + (result.amount() != null ? "<br>Số tiền: " + result.amount().toBigInteger() + " đ" : "") + "</p>"
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