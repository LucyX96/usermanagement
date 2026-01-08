package com.company.usermanagement;

import com.company.usermanagement.configuration.security.JwtProperties;
import com.company.usermanagement.security.service.JwtService;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void accessToken_shouldContainIssuerAudienceType() {
        JwtProperties props = new JwtProperties(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZg==",
                Duration.ofMinutes(2),
                Duration.ofMinutes(10),
                "usermanagement-api",
                "usermanagement-client",
                "Authorization",
                "Bearer"
        );

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        JwtService jwt = new JwtService(key, props);

        String token = jwt.generateAccessToken("luciano", Map.of("x", "y"));

        assertThat(jwt.extractUsername(token)).isEqualTo("luciano");
        assertThat(jwt.extractType(token)).isEqualTo("access");
        assertThat(jwt.extractJti(token)).isNotBlank();
    }

    @Test
    void refreshToken_shouldBeTypeRefresh() {
        JwtProperties props = new JwtProperties(
                "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZg==",
                Duration.ofMinutes(2),
                Duration.ofMinutes(10),
                "usermanagement-api",
                "usermanagement-client",
                "Authorization",
                "Bearer"
        );

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        JwtService jwt = new JwtService(key, props);

        String token = jwt.generateRefreshToken("luciano");

        assertThat(jwt.extractUsername(token)).isEqualTo("luciano");
        assertThat(jwt.extractType(token)).isEqualTo("refresh");
        assertThat(jwt.extractJti(token)).isNotBlank();
    }
}
