package com.company.usermanagement.security.utility;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${security.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms:3600000}") // 1h default
    private long jwtExpirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    public String generateToken(
            String subject,
            Map<String, Object> claims,
            Long customExpirationMs
    ) {
        long expiration = customExpirationMs != null
                ? customExpirationMs
                : jwtExpirationMs;

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateToken(UserDetails user) {
        return generateToken(
                user.getUsername(),
                Map.of("roles", user.getAuthorities()),
                null
        );
    }

}
