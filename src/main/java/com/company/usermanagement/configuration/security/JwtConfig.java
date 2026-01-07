package com.company.usermanagement.configuration.security;

import java.util.Base64;
import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    private final JwtProperties jwtProperties;

    public JwtConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
