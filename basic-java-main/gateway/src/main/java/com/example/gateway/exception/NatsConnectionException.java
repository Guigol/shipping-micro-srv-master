package com.example.gateway.exception;

public class NatsConnectionException extends RuntimeException {
    
    public NatsConnectionException(String message) {
        super(message);
    }
    
    public NatsConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}