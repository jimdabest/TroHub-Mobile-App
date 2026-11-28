package me.ihqqq.trohub.configuration;

import me.ihqqq.trohub.service.AuthenticationService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final AuthenticationService authenticationService;
    private final NimbusJwtDecoder nimbusJwtDecoder;

    public CustomJwtDecoder(AuthenticationService authenticationService, JwtProperties jwtProperties) {
        this.authenticationService = authenticationService;
        this.nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(jwtProperties.signerKeyBytes(), "HS512"))
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (!authenticationService.isTokenValid(token)) {
            throw new JwtException("Token invalid");
        }
        return nimbusJwtDecoder.decode(token);
    }
}