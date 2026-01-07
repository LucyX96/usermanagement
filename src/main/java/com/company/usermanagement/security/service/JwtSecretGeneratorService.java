package com.company.usermanagement.security.service;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtSecretGeneratorService {

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String generateHs512Secret() {
        return Encoders.BASE64.encode(
                Keys.secretKeyFor(SIGNATURE_ALGORITHM).getEncoded()
        );
    }
}
