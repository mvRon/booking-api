package com.mvron.booking.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtTokenServiceTest {

    private final SecretKey key = new SecretKeySpec(
            "test-secret-key-at-least-32-bytes-long!!".getBytes(StandardCharsets.UTF_8),
            "HmacSHA256");
    private final JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).build();
    private final JwtTokenService service = new JwtTokenService(encoder);

    @Test
    void issuedTokenCanBeDecodedAndCarriesClaims() {
        String token = service.issueToken("admin@example.com", "ADMIN");
        System.out.println("token = " + token);

        Jwt jwt = decoder.decode(token);

        assertEquals("admin@example.com", jwt.getSubject());
        assertEquals("ADMIN", jwt.getClaimAsString("role"));
        assertEquals(Duration.ofHours(1), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        String token = service.issueToken("admin@example.com", "ADMIN");
        SecretKey otherKey = new SecretKeySpec(
                "another-secret-key-at-least-32-bytes!!".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        JwtDecoder otherDecoder = NimbusJwtDecoder.withSecretKey(otherKey).build();

        assertThrows(JwtException.class, () -> otherDecoder.decode(token));
    }

    @Test
    void tokenWithTamperedPayloadIsRejected() {
        String userToken = service.issueToken("user@example.com", "USER");
        String adminToken = service.issueToken("admin@example.com", "ADMIN");

        String[] userParts = userToken.split("\\.");
        String[] adminParts = adminToken.split("\\.");
        String forged = userParts[0] + "." + adminParts[1] + "." + userParts[2];

        assertThrows(JwtException.class, () -> decoder.decode(forged));
    }
}