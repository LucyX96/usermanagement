package com.company.usermanagement.security.controller;

import com.company.usermanagement.security.dto.JwtCustomRequest;
import com.company.usermanagement.security.service.JwtSecretGeneratorService;
import com.company.usermanagement.security.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/jwt/generate")
public class JwtTestController {

    private final JwtService jwtService;
    private final JwtSecretGeneratorService secretService;

    public JwtTestController(JwtService jwtService, JwtSecretGeneratorService secretService) {
        this.jwtService = jwtService;
        this.secretService = secretService;
    }

    @PostMapping("/jwtToken")
    public ResponseEntity<Map<String, String>> generateCustomToken(@Valid @RequestBody JwtCustomRequest request) {
        Duration exp = request.getExpirationMs() != null ? Duration.ofMillis(request.getExpirationMs()) : null;

        String token = jwtService.generateAccessToken(
                request.getSubject(),
                request.getClaims(),
                exp
        );

        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/secretKey")
    public ResponseEntity<Map<String, String>> generateSecret() {
        return ResponseEntity.ok(Map.of("secret", secretService.generateHs512Secret()));
    }
}
