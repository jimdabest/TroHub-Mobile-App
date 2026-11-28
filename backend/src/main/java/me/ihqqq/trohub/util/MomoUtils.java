package me.ihqqq.trohub.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public final class MomoUtils {
    private MomoUtils() {}

    private static final List<String> NOTIFICATION_KEYS = List.of(
            "amount", "extraData", "message", "orderId", "orderInfo", "orderType",
            "partnerCode", "payType", "requestId", "responseTime", "resultCode", "transId");

    public static String createRaw(String accessKey, String amount, String extraData, String ipnUrl, String orderId,
                                   String orderInfo, String partnerCode, String redirectUrl, String requestId,
                                   String requestType) {
        return "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId + "&orderInfo=" + orderInfo + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl + "&requestId=" + requestId + "&requestType=" + requestType;
    }

    public static String notificationRaw(String accessKey, Map<String, String> fields) {
        StringBuilder raw = new StringBuilder("accessKey=").append(accessKey);
        for (String key : NOTIFICATION_KEYS) {
            raw.append('&').append(key).append('=').append(fields.getOrDefault(key, ""));
        }
        return raw.toString();
    }

    public static String sign(String secretKey, String raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Không thể ký HMAC-SHA256", e);
        }
    }

    public static boolean verify(String secretKey, String raw, String receivedSignature) {
        if (receivedSignature == null) return false;
        byte[] expected = sign(secretKey, raw).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = receivedSignature.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }
}