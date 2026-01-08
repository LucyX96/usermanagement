package com.company.usermanagement.configuration.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    private final JwtProperties props;

    public JwtConfig(JwtProperties props) {
        this.props = props;
    }

    @Bean
    public SecretKey jwtSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(props.secret());
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        int bits = key.getEncoded().length * 8;
        if (bits < 512) {
            throw new IllegalStateException(
                    "JWT secret troppo corta: " + bits + " bits. HS512 richiede >= 512 bits."
            );
        }
        return key;
    }
}
