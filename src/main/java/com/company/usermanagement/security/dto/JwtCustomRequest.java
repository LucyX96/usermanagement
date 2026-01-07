package com.company.usermanagement.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class JwtCustomRequest {

    @NotBlank
    private String subject;

    private Map<String, Object> claims;

    private Long expirationMs;
}
