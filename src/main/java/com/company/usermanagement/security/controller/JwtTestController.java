package com.company.usermanagement.security.controller;

import com.company.usermanagement.security.dto.JwtCustomRequest;
import com.company.usermanagement.security.service.JwtSecretGeneratorService;
import com.company.usermanagement.security.utility.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/jwt/generate")
public class JwtTestController {

    private final JwtUtils jwtUtils;
    private final JwtSecretGeneratorService secretService;

    public JwtTestController(JwtUtils jwtUtils, JwtSecretGeneratorService secretService) {
        this.jwtUtils = jwtUtils;
        this.secretService = secretService;
    }

    @PostMapping("/jwtToken")
    public ResponseEntity<Map<String, String>> generateCustomJwt(
            @Valid @RequestBody JwtCustomRequest request,
            Authentication authentication
    ) {
        // autenticato garantito da Spring Security
        String token = jwtUtils.generateToken(
                request.getSubject(),
                request.getClaims(),
                request.getExpirationMs()
        );

        return ResponseEntity.ok(
                Map.of("token", token)
        );
    }

    @PostMapping("/secretKey")
    public ResponseEntity<Map<String, String>> generateSecret() {
        return ResponseEntity.ok(
                Map.of("secret", secretService.generateHs512Secret())
        );
    }
}
