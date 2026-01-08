package com.company.usermanagement.exception;

import org.springframework.http.HttpStatus;

public class RegistrationException extends ApiException {
    public RegistrationException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.REGISTRATION_ERROR);
    }
}
