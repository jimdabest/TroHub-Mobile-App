package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.configuration.MomoProperties;
import me.ihqqq.trohub.dto.request.MomoIpnRequest;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.MomoCreateResponse;
import me.ihqqq.trohub.dto.response.OnlinePaymentResponse;
import me.ihqqq.trohub.enums.IpnResult;
import me.ihqqq.trohub.enums.PaymentMethod;
import me.ihqqq.trohub.enums.PaymentProvider;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.util.MomoUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MomoService {

    static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(1_000);
    static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(50_000_000);
    static final Set<Integer> PENDING_CODES = Set.of(7000, 7002, 9000);
    static final int USER_DENIED_CODE = 1006;

    MomoProperties props;
    RestClient momoRestClient;
    OnlinePaymentSupport support;
    PropertyAccessService accessService;


    public OnlinePaymentResponse createPaymentUrl(UUID propertyId, UUID invoiceId, OnlinePaymentRequest request) {
        accessService.requireMember(propertyId);
        return buildPaymentUrl(propertyId, invoiceId, request);
    }

    public OnlinePaymentResponse createPaymentUrlForTenant(UUID propertyId, UUID invoiceId, OnlinePaymentRequest request) {
        return buildPaymentUrl(propertyId, invoiceId, request);
    }

    private OnlinePaymentResponse buildPaymentUrl(UUID propertyId, UUID invoiceId, OnlinePaymentRequest request) {
        if (!props.isConfigured()) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_NOT_CONFIGURED);
        }

        String orderId = UUID.randomUUID().toString().replace("-", "");
        String requestId = UUID.randomUUID().toString().replace("-", "");

        var pending = support.preparePending(propertyId, invoiceId,
                request != null ? request.getAmount() : null, PaymentProvider.MOMO, orderId, MIN_AMOUNT, MAX_AMOUNT);

        String amount = pending.amount().toBigInteger().toString();
        String orderInfo = "Thanh toan hoa don phong " + pending.roomCode().replaceAll("[^A-Za-z0-9 ]", "");
        String extraData = "";
        String requestType = "captureWallet";
        String signature = MomoUtils.sign(props.secretKey(), MomoUtils.createRaw(props.accessKey(), amount, extraData,
                props.ipnUrl(), orderId, orderInfo, props.partnerCode(), props.redirectUrl(), requestId, requestType));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", props.partnerCode());
        body.put("requestId", requestId);
        body.put("amount", pending.amount().longValueExact());
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", props.redirectUrl());
        body.put("ipnUrl", props.ipnUrl());
        body.put("requestType", requestType);
        body.put("extraData", extraData);
        body.put("lang", "vi");
        body.put("signature", signature);

        MomoCreateResponse response;
        try {
            response = momoRestClient.post().uri(props.endpoint())
                    .contentType(MediaType.APPLICATION_JSON).body(body)
                    .retrieve().body(MomoCreateResponse.class);
        } catch (RestClientException e) {
            log.error("Gọi MoMo tạo giao dịch {} thất bại", orderId, e);
            support.markFailed(pending.transactionId(), "Lỗi gọi MoMo: " + e.getMessage());
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        if (response == null || response.resultCode() == null || response.resultCode() != 0
                || response.payUrl() == null || response.payUrl().isBlank()) {
            log.error("MoMo từ chối giao dịch {}: {}", orderId, response);
            support.markFailed(pending.transactionId(), "MoMo từ chối: " + response);
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }

        return OnlinePaymentResponse.builder()
                .transactionId(pending.transactionId())
                .txnRef(orderId)
                .paymentUrl(response.payUrl())
                .deeplink(response.deeplink())
                .amount(pending.amount())
                .build();
    }

    public IpnResult handleIpn(MomoIpnRequest ipn) {
        if (!props.isConfigured() || ipn == null || ipn.orderId() == null || ipn.amount() == null
                || ipn.resultCode() == null) {
            return IpnResult.INVALID_SIGNATURE;
        }

        Map<String, String> fields = new HashMap<>();
        put(fields, "amount", ipn.amount());
        put(fields, "extraData", ipn.extraData());
        put(fields, "message", ipn.message());
        put(fields, "orderId", ipn.orderId());
        put(fields, "orderInfo", ipn.orderInfo());
        put(fields, "orderType", ipn.orderType());
        put(fields, "partnerCode", ipn.partnerCode());
        put(fields, "payType", ipn.payType());
        put(fields, "requestId", ipn.requestId());
        put(fields, "responseTime", ipn.responseTime());
        put(fields, "resultCode", ipn.resultCode());
        put(fields, "transId", ipn.transId());
        if (!MomoUtils.verify(props.secretKey(), MomoUtils.notificationRaw(props.accessKey(), fields), ipn.signature())
                || !props.partnerCode().equals(ipn.partnerCode())) {
            return IpnResult.INVALID_SIGNATURE;
        }

        boolean pending = PENDING_CODES.contains(ipn.resultCode());
        boolean success = ipn.resultCode() == 0;
        return support.settle(PaymentProvider.MOMO, ipn.orderId(), BigDecimal.valueOf(ipn.amount()), pending, success,
                String.valueOf(ipn.transId()), fields.toString(), PaymentMethod.MOMO);
    }


    public record ReturnResult(boolean validSignature, boolean success, boolean cancelled,
                               String resultCode, String orderId, BigDecimal amount) {}

    public ReturnResult verifyReturn(Map<String, String> params) {
        boolean valid = props.isConfigured() && MomoUtils.verify(props.secretKey(),
                MomoUtils.notificationRaw(props.accessKey(), params), params.get("signature"));
        if (!valid) {
            return new ReturnResult(false, false, false, null, null, null);
        }
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(params.getOrDefault("amount", ""));
        } catch (NumberFormatException ignored) {

        }
        String code = params.get("resultCode");
        return new ReturnResult(true, "0".equals(code), String.valueOf(USER_DENIED_CODE).equals(code),
                code, params.get("orderId"), amount);
    }

    private static void put(Map<String, String> map, String key, Object value) {
        map.put(key, value == null ? "" : String.valueOf(value));
    }
}