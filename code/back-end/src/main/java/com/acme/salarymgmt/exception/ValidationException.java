package com.acme.salarymgmt.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> fieldErrors;

    public ValidationException(String message, List<String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = List.of();
    }

    public List<String> getFieldErrors() { return fieldErrors; }
}
