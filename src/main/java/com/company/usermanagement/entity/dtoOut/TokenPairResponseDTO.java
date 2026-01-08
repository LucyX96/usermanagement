package com.company.usermanagement.entity.dtoOut;

public record TokenPairResponseDTO(
        String accessToken,
        String refreshToken
) {}
