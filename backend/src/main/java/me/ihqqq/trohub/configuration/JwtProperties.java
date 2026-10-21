package me.ihqqq.trohub.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String signerKey, long validDuration, long refreshableDuration) {

    public JwtProperties {
        if (signerKey == null || signerKey.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalStateException("jwt.signer-key (JWT_SIGNER_KEY) phai co it nhat 64 ky tu cho HS512");
        }
    }

    public byte[] signerKeyBytes() {
        return signerKey.getBytes(StandardCharsets.UTF_8);
    }
}