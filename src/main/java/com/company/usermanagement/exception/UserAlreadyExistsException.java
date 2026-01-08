package com.company.usermanagement.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, ErrorCode.USER_ALREADY_EXISTS);
    }
}
