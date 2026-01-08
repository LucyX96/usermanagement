package com.company.usermanagement.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
}
