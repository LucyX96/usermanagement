package com.company.usermanagement.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiErrorResponse(
                        ex.getStatus().value(),
                        ex.getErrorCode().name(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        // uniforma anche questa, senza spargere UsernameNotFoundException in giro
        return ResponseEntity
                .status(404)
                .body(new ApiErrorResponse(
                        404,
                        ErrorCode.USER_NOT_FOUND.name(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        400,
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Richiesta non valida",
                        fieldErrors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> violations = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "param";
            violations.putIfAbsent(path, v.getMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        400,
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Parametri non validi",
                        violations
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        LOGGER.error("Unhandled exception", ex);

        return ResponseEntity
                .status(500)
                .body(new ApiErrorResponse(
                        500,
                        ErrorCode.INTERNAL_ERROR.name(),
                        "Errore interno"
                ));
    }
}
