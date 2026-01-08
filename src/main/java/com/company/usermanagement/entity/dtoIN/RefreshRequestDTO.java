package com.company.usermanagement.entity.dtoIN;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
        @NotBlank(message = "refreshToken obbligatorio")
        String refreshToken
) {}

