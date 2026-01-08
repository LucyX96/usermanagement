package com.company.usermanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        Object details
) {
    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now(), null);
    }

    public ApiErrorResponse(int status, String error, String message, Object details) {
        this(status, error, message, LocalDateTime.now(), details);
    }
}
