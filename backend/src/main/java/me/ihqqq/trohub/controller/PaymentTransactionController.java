package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.PageResponse;
import me.ihqqq.trohub.dto.response.PaymentTransactionResponse;
import me.ihqqq.trohub.enums.PaymentTransactionStatus;
import me.ihqqq.trohub.service.PaymentTransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/payment-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payment transactions")
public class PaymentTransactionController {

    PaymentTransactionService paymentTransactionService;

    @GetMapping
    @Operation(summary = "Danh sách giao dịch cổng thanh toán (lọc theo status, vd SUCCESS_UNAPPLIED để đối soát)")
    ApiResponse<PageResponse<PaymentTransactionResponse>> list(@PathVariable UUID propertyId,
                                                               @RequestParam(required = false) PaymentTransactionStatus status,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(paymentTransactionService.list(propertyId, status, page, size));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Tra trạng thái một giao dịch (app gọi định kỳ sau khi mở trang thanh toán)")
    ApiResponse<PaymentTransactionResponse> get(@PathVariable UUID propertyId, @PathVariable UUID transactionId) {
        return ApiResponse.ok(paymentTransactionService.get(propertyId, transactionId));
    }

    @PostMapping("/{transactionId}/reconcile")
    @Operation(summary = "Đánh dấu đã xử lý giao dịch thu tiền nhưng không ghi nhận được vào hóa đơn")
    ApiResponse<PaymentTransactionResponse> reconcile(@PathVariable UUID propertyId, @PathVariable UUID transactionId) {
        return ApiResponse.ok(paymentTransactionService.reconcile(propertyId, transactionId));
    }
}