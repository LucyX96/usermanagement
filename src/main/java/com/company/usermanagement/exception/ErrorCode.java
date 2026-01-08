package com.company.usermanagement.exception;

public enum ErrorCode {
    // Validation
    VALIDATION_ERROR,

    // Auth/Security
    UNAUTHORIZED,
    FORBIDDEN,

    // Domain/User
    USER_ALREADY_EXISTS,
    USER_NOT_FOUND,
    REGISTRATION_ERROR,

    // Generic
    INTERNAL_ERROR
}
