package me.ihqqq.trohub.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "momo")
public record MomoProperties(String partnerCode, String accessKey, String secretKey,
                             String endpoint, String redirectUrl, String ipnUrl) {
    public boolean isConfigured() {
        return notBlank(partnerCode) && notBlank(accessKey) && notBlank(secretKey)
                && notBlank(endpoint) && notBlank(redirectUrl) && notBlank(ipnUrl);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}