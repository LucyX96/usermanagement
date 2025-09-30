package com.company.usermanagement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record JwtProperties(String secret, String param, String prefix) {
}