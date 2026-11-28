package me.ihqqq.trohub.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.net.URLEncoder;

public final class VnPayUtils {
    private VnPayUtils() {}
    public static String hashData(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner("&");
        new TreeMap<>(fields).forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                joiner.add(k + "=" + URLEncoder.encode(v, StandardCharsets.US_ASCII));
            }
        });
        return joiner.toString();
    }

    public static String query(Map<String, String> fields) {
        StringJoiner joiner = new StringJoiner("&");
        new TreeMap<>(fields).forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                joiner.add(URLEncoder.encode(k, StandardCharsets.US_ASCII) + "="
                        + URLEncoder.encode(v, StandardCharsets.US_ASCII));
            }
        });
        return joiner.toString();
    }

    public static String sign(String secret, Map<String, String> fields) {
        return hmacSha512(secret, hashData(fields));
    }

    public static boolean verify(String secret, Map<String, String> fields, String receivedHash) {
        byte[] expected = sign(secret, fields).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = receivedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    static String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Không thể ký HMAC-SHA512", e);
        }
    }
}