package me.ihqqq.trohub.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.dto.request.MomoIpnRequest;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.ApiResponse;
import me.ihqqq.trohub.dto.response.OnlinePaymentResponse;
import me.ihqqq.trohub.enums.IpnResult;
import me.ihqqq.trohub.service.MomoService;
import me.ihqqq.trohub.util.PaymentResultPage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "MoMo")
public class MomoController {

    MomoService momoService;

    @PostMapping("/properties/{propertyId}/invoices/{invoiceId}/momo/create-url")
    ApiResponse<OnlinePaymentResponse> createUrl(@PathVariable UUID propertyId, @PathVariable UUID invoiceId,
                                                 @RequestBody(required = false) OnlinePaymentRequest request) {
        return ApiResponse.ok(momoService.createPaymentUrl(propertyId, invoiceId, request));
    }

    @PostMapping(value = "/payments/momo/ipn", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> ipn(@RequestBody MomoIpnRequest body) {
        try {
            IpnResult result = momoService.handleIpn(body);
            if (!result.isAcknowledged()) {
                log.warn("MoMo IPN bị từ chối: {}", result);
            }
            return result.isAcknowledged() ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi xử lý MoMo IPN", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/payments/momo/return", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    String returnPage(HttpServletRequest http) {
        Map<String, String> params = new HashMap<>();
        http.getParameterMap().forEach((k, v) -> {
            if (v.length > 0) params.put(k, v[0]);
        });
        var r = momoService.verifyReturn(params);
        return PaymentResultPage.render(r.validSignature(), r.success(), r.cancelled(), r.resultCode(), r.orderId(), r.amount());
    }
}