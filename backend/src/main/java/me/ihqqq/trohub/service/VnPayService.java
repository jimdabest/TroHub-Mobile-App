package me.ihqqq.trohub.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.configuration.VnPayProperties;
import me.ihqqq.trohub.constant.AppConstant;
import me.ihqqq.trohub.dto.request.OnlinePaymentRequest;
import me.ihqqq.trohub.dto.response.OnlinePaymentResponse;
import me.ihqqq.trohub.enums.PaymentMethod;
import me.ihqqq.trohub.enums.IpnResult;
import me.ihqqq.trohub.enums.PaymentProvider;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.util.VnPayUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayService {

    static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(5_000);
    static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(999_999_999);

    VnPayProperties props;
    OnlinePaymentSupport support;
    PropertyAccessService accessService;


    public OnlinePaymentResponse createPaymentUrl(UUID propertyId, UUID invoiceId,
                                                  OnlinePaymentRequest request, String clientIp) {
        accessService.requireMember(propertyId);
        return buildPaymentUrl(propertyId, invoiceId, request, clientIp);
    }

    public OnlinePaymentResponse createPaymentUrlForTenant(UUID propertyId, UUID invoiceId,
                                                           OnlinePaymentRequest request, String clientIp) {
        return buildPaymentUrl(propertyId, invoiceId, request, clientIp);
    }

    private OnlinePaymentResponse buildPaymentUrl(UUID propertyId, UUID invoiceId,
                                                  OnlinePaymentRequest request, String clientIp) {
        if (!props.isConfigured()) {
            throw new AppException(ErrorCode.PAYMENT_GATEWAY_NOT_CONFIGURED);
        }

        String txnRef = UUID.randomUUID().toString().replace("-", "");
        var pending = support.preparePending(propertyId, invoiceId,
                request != null ? request.getAmount() : null, PaymentProvider.VNPAY, txnRef, MIN_AMOUNT, MAX_AMOUNT);
        BigDecimal amount = pending.amount();

        LocalDateTime now = LocalDateTime.now(AppConstant.BUSINESS_ZONE);
        LocalDateTime expire = now.plusMinutes(props.expireMinutes());

        Map<String, String> fields = new TreeMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TmnCode", props.tmnCode());
        fields.put("vnp_Amount", amount.movePointRight(2).toBigInteger().toString()); // VNPay nhân 100
        fields.put("vnp_CurrCode", "VND");
        fields.put("vnp_TxnRef", txnRef);
        fields.put("vnp_OrderInfo", "Thanh toan hoa don phong "
                + pending.roomCode().replaceAll("[^A-Za-z0-9 ]", ""));
        fields.put("vnp_OrderType", "other");
        fields.put("vnp_Locale", "vn");
        fields.put("vnp_ReturnUrl", props.returnUrl());
        fields.put("vnp_IpAddr", clientIp);
        fields.put("vnp_CreateDate", now.format(VNP_DATE));
        fields.put("vnp_ExpireDate", expire.format(VNP_DATE));

        String url = props.payUrl() + "?" + VnPayUtils.query(fields)
                + "&vnp_SecureHash=" + VnPayUtils.sign(props.hashSecret(), fields);

        return OnlinePaymentResponse.builder()
                .transactionId(pending.transactionId())
                .txnRef(txnRef)
                .paymentUrl(url)
                .amount(amount)
                .expiresAt(Instant.now().plusSeconds(props.expireMinutes() * 60))
                .build();
    }


    public Map<String, String> handleIpn(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        Map<String, String> fields = signedFields(params);

        if (!hasValidSignature(fields, received)) {
            return response("97", "Invalid signature");
        }

        BigDecimal paid;
        try {
            paid = new BigDecimal(fields.getOrDefault("vnp_Amount", "")).movePointLeft(2);
        } catch (NumberFormatException e) {
            return response("04", "Invalid amount");
        }

        boolean success = "00".equals(fields.get("vnp_ResponseCode"))
                && "00".equals(fields.get("vnp_TransactionStatus"));
        IpnResult result = support.settle(PaymentProvider.VNPAY, fields.getOrDefault("vnp_TxnRef", ""), paid,
                false, success, fields.get("vnp_TransactionNo"), VnPayUtils.query(fields), PaymentMethod.VNPAY);

        return switch (result) {
            case SETTLED -> response("00", "Confirm Success");
            case ALREADY_PROCESSED, IGNORED_PENDING -> response("02", "Order already confirmed");
            case NOT_FOUND -> response("01", "Order not found");
            case AMOUNT_MISMATCH -> response("04", "Invalid amount");
            case INVALID_SIGNATURE -> response("97", "Invalid signature");
        };
    }


    public record ReturnResult(boolean validSignature, boolean success, String responseCode,
                               String txnRef, BigDecimal amount) {}


    public ReturnResult verifyReturn(Map<String, String> params) {
        Map<String, String> fields = signedFields(params);
        if (!hasValidSignature(fields, params.get("vnp_SecureHash"))) {
            return new ReturnResult(false, false, null, null, null);
        }
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(fields.getOrDefault("vnp_Amount", "")).movePointLeft(2);
        } catch (NumberFormatException ignored) {

        }
        boolean success = "00".equals(fields.get("vnp_ResponseCode"))
                && "00".equals(fields.get("vnp_TransactionStatus"));
        return new ReturnResult(true, success, fields.get("vnp_ResponseCode"), fields.get("vnp_TxnRef"), amount);
    }

    private static Map<String, String> signedFields(Map<String, String> params) {
        Map<String, String> fields = new TreeMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                fields.put(k, v);
            }
        });
        return fields;
    }

    private boolean hasValidSignature(Map<String, String> fields, String received) {
        return props.isConfigured() && received != null && VnPayUtils.verify(props.hashSecret(), fields, received);
    }

    public static Map<String, String> response(String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("RspCode", code);
        body.put("Message", message);
        return body;
    }
}