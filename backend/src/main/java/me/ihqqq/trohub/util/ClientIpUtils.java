package me.ihqqq.trohub.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtils {
    private ClientIpUtils() {}

    public static String resolve(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : http.getRemoteAddr();
        return ip.contains(":") ? "127.0.0.1" : ip;
    }
}