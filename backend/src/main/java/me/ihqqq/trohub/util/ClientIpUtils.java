package me.ihqqq.trohub.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtils {
    private ClientIpUtils() {}

    public static String resolve(HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }
        // VNPay yêu cầu IPv4; IPv6 thật (hoặc ::1) quy về 127.0.0.1
        return ip.contains(":") ? "127.0.0.1" : ip;
    }
}