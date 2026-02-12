package com.example.userservice.exception;

import com.example.userservice.dto.NatsResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public NatsResponse handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return NatsResponse.error("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseBody
    public NatsResponse handleDuplicateEmailException(DuplicateEmailException ex) {
        log.error("Duplicate email: {}", ex.getMessage());
        return NatsResponse.error("DUPLICATE_EMAIL", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public NatsResponse handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        log.error("Validation error: {}", message);
        return NatsResponse.error("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public NatsResponse handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        return NatsResponse.error("VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public NatsResponse handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Database constraint violation: {}", ex.getMessage());
        String message = "Database constraint violation";
        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            message = "Email already exists";
        }
        return NatsResponse.error("DATABASE_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public NatsResponse handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return NatsResponse.error("INTERNAL_ERROR", "An unexpected error occurred: " + ex.getMessage());
    }
}