package me.ihqqq.trohub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MomoCreateResponse(String partnerCode, String orderId, String requestId, Long amount,
                                 Long responseTime, String message, Integer resultCode,
                                 String payUrl, String deeplink, String qrCodeUrl) {}