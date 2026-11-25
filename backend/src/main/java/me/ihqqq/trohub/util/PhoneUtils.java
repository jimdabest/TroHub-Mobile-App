package me.ihqqq.trohub.util;

public final class PhoneUtils {
    private PhoneUtils() {}

    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String d = raw.replaceAll("[^0-9+]", "");
        if (d.length() < 9) {
            return null;
        }
        if (d.matches("\\+84[0-9]{9}")) {
            return "0" + d.substring(3);
        }
        if (d.matches("84[0-9]{9}")) {
            return "0" + d.substring(2);
        }
        return d;
    }
}