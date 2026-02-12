package com.example.gateway.exception;

public class NatsTimeoutException extends RuntimeException {
    
    public NatsTimeoutException(String message) {
        super(message);
    }
    
    public NatsTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}