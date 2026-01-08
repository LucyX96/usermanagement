package com.company.usermanagement.security.service;

import com.company.usermanagement.configuration.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtProperties props;

    public JwtService(SecretKey jwtSecretKey, JwtProperties props) {
        this.secretKey = jwtSecretKey;
        this.props = props;
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(user.getUsername(), Map.of("roles", user.getAuthorities()));
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, props.expiration(), "access");
    }

    public String generateAccessToken(String subject, Map<String, Object> claims, Duration expiration) {
        return buildToken(subject, claims, expiration != null ? expiration : props.expiration(), "access");
    }


    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of(), props.refreshExpiration(), "refresh");
    }

    private String buildToken(String subject, Map<String, Object> claims, Duration expiration, String type) {
        Date now = new Date();
        long expMs = expiration.toMillis();

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())     // jti
                .setSubject(subject)
                .setIssuer(props.issuer())               // iss
                .setAudience(props.audience())           // aud
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expMs))
                .claim("typ", type)                      // access/refresh
                .addClaims(claims != null ? claims : Map.of())
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractType(String token) {
        Object typ = extractAllClaims(token).get("typ");
        return typ != null ? typ.toString() : null;
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "access");
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("typ"));
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        final String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && expectedType.equals(extractType(token))
                && validateStandardClaims(token);
    }

    private boolean validateStandardClaims(String token) {
        Claims c = extractAllClaims(token);
        if (c.getExpiration() == null || c.getExpiration().before(new Date())) return false;
        if (!props.issuer().equals(c.getIssuer())) return false;

        // aud può essere String o Collection; qui gestiamo il caso String (jjwt classico)
        Object aud = c.getAudience();
        if (aud == null || !props.audience().equals(aud.toString())) return false;

        return true;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
