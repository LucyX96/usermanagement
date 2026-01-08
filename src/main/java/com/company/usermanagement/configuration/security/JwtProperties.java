package com.company.usermanagement.configuration.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String secret,              // Base64
        @NotNull Duration expiration,          // access token (es: PT15M)
        @NotNull Duration refreshExpiration,   // refresh token (es: P7D)
        @NotBlank String issuer,               // es: usermanagement-api
        @NotBlank String audience,             // es: usermanagement-client
        @NotBlank String header,               // Authorization
        @NotBlank String prefix                // Bearer
) {}
