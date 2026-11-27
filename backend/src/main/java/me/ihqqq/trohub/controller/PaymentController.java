package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.PaymentRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.dto.response.PaymentCreationResponse;
import me.ihqqq.trohub.dto.response.PaymentResponse;
import me.ihqqq.trohub.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/invoices/{invoiceId}/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payments")
public class PaymentController {

    PaymentService paymentService;

    @PostMapping
    ApiResponse<PaymentCreationResponse> create(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                                @RequestBody @Valid PaymentRequest request) {
        return ApiResponse.ok(paymentService.create(propertyId, invoiceId, request));
    }

    @DeleteMapping("/{paymentId}")
    @Operation(summary = "Hủy khoản thanh toán ghi nhận thủ công (chỉ OWNER; không áp dụng cho VNPay/MoMo)")
    ApiResponse<InvoiceSummaryResponse> voidPayment(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                                    @PathVariable UUID paymentId) {
        return ApiResponse.ok(paymentService.voidPayment(propertyId, invoiceId, paymentId));
    }

    @GetMapping
    ApiResponse<List<PaymentResponse>> list(@PathVariable UUID propertyId, @PathVariable UUID invoiceId) {
        return ApiResponse.ok(paymentService.list(propertyId, invoiceId));
    }
}