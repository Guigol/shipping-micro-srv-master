package com.example.userservice.exception;

public class DuplicateEmailException extends RuntimeException {
    
    public DuplicateEmailException(String message) {
        super(message);
    }
    
    public DuplicateEmailException(String email, String context) {
        super("Email '" + email + "' already exists" + (context != null ? " " + context : ""));
    }
}