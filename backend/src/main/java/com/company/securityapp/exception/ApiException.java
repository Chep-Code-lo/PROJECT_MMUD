package com.company.securityapp.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

