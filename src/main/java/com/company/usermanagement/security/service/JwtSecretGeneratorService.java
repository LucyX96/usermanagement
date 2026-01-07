package com.company.usermanagement.security.service;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtSecretGeneratorService {

    public String generateHs512Secret() {
        return Encoders.BASE64.encode(
                Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded()
        );
    }
}
