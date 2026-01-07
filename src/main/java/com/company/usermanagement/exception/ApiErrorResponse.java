package com.company.usermanagement.exception;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ApiErrorResponse(int status, String error, String message, LocalDateTime timestamp) {

    public ApiErrorResponse(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now());
    }

}
