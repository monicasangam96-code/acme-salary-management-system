package com.acme.salarymgmt.exception;

public class InsufficientPermissionsException extends RuntimeException {
    public InsufficientPermissionsException(String message) { super(message); }
}
