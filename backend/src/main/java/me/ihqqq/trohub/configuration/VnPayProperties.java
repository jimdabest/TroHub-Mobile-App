package me.ihqqq.trohub.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public record VnPayProperties(String tmnCode, String hashSecret, String payUrl, String returnUrl, long expireMinutes) {

    public VnPayProperties {
        if (expireMinutes <= 0) {
            expireMinutes = 15;
        }
    }

    public boolean isConfigured() {
        return notBlank(tmnCode) && notBlank(hashSecret) && notBlank(payUrl) && notBlank(returnUrl);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}