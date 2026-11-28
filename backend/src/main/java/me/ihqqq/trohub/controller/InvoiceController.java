package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.trohub.dto.request.InvoiceGenerationRequest;
import me.ihqqq.trohub.dto.request.InvoiceItemRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.InvoiceGenerationResponse;
import me.ihqqq.trohub.dto.response.InvoiceResponse;
import me.ihqqq.trohub.dto.response.InvoiceSummaryResponse;
import me.ihqqq.trohub.enums.InvoiceStatusFilter;
import me.ihqqq.trohub.service.InvoiceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Invoices")
public class InvoiceController {

    InvoiceService invoiceService;

    @PostMapping("/generate")
    ApiResponse<InvoiceGenerationResponse> generate(@PathVariable UUID propertyId,
                                                    @RequestBody @Valid InvoiceGenerationRequest request) {
        return ApiResponse.ok(invoiceService.generate(propertyId, request));
    }

    @GetMapping
    ApiResponse<List<InvoiceSummaryResponse>> list(
            @PathVariable UUID propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate period,
            @RequestParam(defaultValue = "ALL") InvoiceStatusFilter status) {
        return ApiResponse.ok(invoiceService.list(propertyId, period, status));
    }

    @GetMapping("/{invoiceId}")
    ApiResponse<InvoiceResponse> get(@PathVariable UUID propertyId, @PathVariable UUID invoiceId) {
        return ApiResponse.ok(invoiceService.get(propertyId, invoiceId));
    }

    @PostMapping("/{invoiceId}/items")
    @Operation(summary = "Thêm khoản phát sinh / giảm giá (đơn giá âm) vào hóa đơn chưa thanh toán đủ (chỉ OWNER)")
    ApiResponse<InvoiceResponse> addItem(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                         @RequestBody @Valid InvoiceItemRequest request) {
        return ApiResponse.ok(invoiceService.addAdjustment(propertyId, invoiceId, request));
    }

    @DeleteMapping("/{invoiceId}/items/{itemId}")
    @Operation(summary = "Xóa khoản phát sinh / giảm giá đã thêm thủ công (không xóa được các khoản tự sinh; chỉ OWNER)")
    ApiResponse<InvoiceResponse> removeItem(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                            @PathVariable UUID itemId) {
        return ApiResponse.ok(invoiceService.removeAdjustment(propertyId, invoiceId, itemId));
    }

    @DeleteMapping("/{invoiceId}")
    ApiResponse<Void> delete(@PathVariable UUID propertyId, @PathVariable UUID invoiceId) {
        invoiceService.delete(propertyId, invoiceId);
        return ApiResponse.ok("Đã xóa hóa đơn");
    }
}