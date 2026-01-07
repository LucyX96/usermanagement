package com.company.usermanagement.exception;

import java.io.Serial;

public class RegistrationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RegistrationException(String message) {
        super(message);
    }
}
