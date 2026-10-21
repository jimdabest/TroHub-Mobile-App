package me.ihqqq.trohub.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.trohub.configuration.JwtProperties;
import me.ihqqq.trohub.dto.request.AuthenticationRequest;
import me.ihqqq.trohub.dto.request.IntrospectRequest;
import me.ihqqq.trohub.dto.request.LogoutRequest;
import me.ihqqq.trohub.dto.request.RefreshRequest;
import me.ihqqq.trohub.dto.response.AuthenticationResponse;
import me.ihqqq.trohub.dto.response.IntrospectResponse;
import me.ihqqq.trohub.entity.InvalidatedToken;
import me.ihqqq.trohub.entity.User;
import me.ihqqq.trohub.exception.AppException;
import me.ihqqq.trohub.exception.ErrorCode;
import me.ihqqq.trohub.repository.InvalidatedTokenRepository;
import me.ihqqq.trohub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    JwtProperties jwtProperties;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String username = request.getUsername().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return AuthenticationResponse.builder().token(generateToken(user)).authenticated(true).build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        return IntrospectResponse.builder().valid(isTokenValid(request.getToken())).build();
    }

    public boolean isTokenValid(String token) {
        try {
            verifyToken(token, false);
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    public void logout(LogoutRequest request) {
        try {
            revoke(verifyToken(request.getToken(), true));
        } catch (AppException e) {
            log.info("Logout voi token da het han hoac khong hop le");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        revoke(signedJWT);

        UUID userId = subjectOf(signedJWT);
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return AuthenticationResponse.builder().token(generateToken(user)).authenticated(true).build();
    }

    private UUID subjectOf(SignedJWT signedJWT) {
        try {
            return UUID.fromString(signedJWT.getJWTClaimsSet().getSubject());
        } catch (ParseException | IllegalArgumentException | NullPointerException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private void revoke(SignedJWT signedJWT) {
        try {
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Instant denyUntil = claims.getIssueTime().toInstant().plusSeconds(jwtProperties.refreshableDuration());
            invalidatedTokenRepository.save(InvalidatedToken.builder()
                    .id(claims.getJWTID())
                    .expiryTime(denyUntil)
                    .build());
        } catch (ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(jwtProperties.signerKeyBytes()))) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (claims.getIssueTime() == null || claims.getExpirationTime() == null || claims.getJWTID() == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            Instant expiry = isRefresh
                    ? claims.getIssueTime().toInstant().plusSeconds(jwtProperties.refreshableDuration())
                    : claims.getExpirationTime().toInstant();
            if (!expiry.isAfter(Instant.now())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            if (invalidatedTokenRepository.existsById(claims.getJWTID())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            return signedJWT;
        } catch (JOSEException | ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String generateToken(User user) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("trohub")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(jwtProperties.validDuration())))
                .jwtID(UUID.randomUUID().toString())
                .claim("username", user.getUsername())
                .claim("scope", "ROLE_" + user.getRole().name())
                .build();

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claims.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(jwtProperties.signerKeyBytes()));
        } catch (JOSEException e) {
            log.error("Cannot sign JWT", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return jwsObject.serialize();
    }
}