package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.LinkTenantRequest;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.*;
import me.ihqqq.trohub.enums.InvoiceStatusFilter;
import me.ihqqq.trohub.service.TenantLinkService;
import me.ihqqq.trohub.service.TenantPortalService;
import me.ihqqq.trohub.util.ClientIpUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenant-portal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Tenant portal")
public class TenantPortalController {

    TenantPortalService tenantPortalService;
    TenantLinkService tenantLinkService;

    @GetMapping("/contracts")
    ApiResponse<List<TenantContractResponse>> contracts() {
        return ApiResponse.ok(tenantPortalService.contracts());
    }

    @GetMapping("/invoices")
    ApiResponse<List<InvoiceSummaryResponse>> invoices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period,
            @RequestParam(defaultValue = "ALL") InvoiceStatusFilter status) {
        return ApiResponse.ok(tenantPortalService.invoices(period, status));
    }

    @GetMapping("/invoices/{invoiceId}")
    ApiResponse<InvoiceResponse> invoice(@PathVariable UUID invoiceId) {
        return ApiResponse.ok(tenantPortalService.invoice(invoiceId));
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    ApiResponse<List<PaymentResponse>> payments(@PathVariable UUID invoiceId) {
        return ApiResponse.ok(tenantPortalService.payments(invoiceId));
    }

    @PostMapping("/link")
    @Operation(summary = "Liên kết tài khoản đang đăng nhập với hồ sơ người thuê bằng mã mời của chủ trọ")
    ApiResponse<LinkedTenantResponse> link(@RequestBody @Valid LinkTenantRequest request) {
        return ApiResponse.ok(tenantLinkService.link(request.getCode()));
    }

    @GetMapping("/payment-transactions/{transactionId}")
    ApiResponse<PaymentTransactionResponse> transaction(@PathVariable UUID transactionId) {
        return ApiResponse.ok(tenantPortalService.transaction(transactionId));
    }

    @PostMapping("/invoices/{invoiceId}/vnpay/create-url")
    ApiResponse<OnlinePaymentResponse> payWithVnPay(@PathVariable UUID invoiceId,
                                                    @RequestBody(required = false) OnlinePaymentRequest request,
                                                    HttpServletRequest http) {
        return ApiResponse.ok(tenantPortalService.payWithVnPay(invoiceId, request, ClientIpUtils.resolve(http)));
    }

    @PostMapping("/invoices/{invoiceId}/momo/create-url")
    ApiResponse<OnlinePaymentResponse> payWithMomo(@PathVariable UUID invoiceId,
                                                   @RequestBody(required = false) OnlinePaymentRequest request) {
        return ApiResponse.ok(tenantPortalService.payWithMomo(invoiceId, request));
    }
}