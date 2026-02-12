package com.example.shippingService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle shipment not found exceptions
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleShipmentNotFound(ShipmentNotFoundException ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.NOT_FOUND.value());
        errorBody.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        errorBody.put("message", ex.getMessage());
        errorBody.put("code", "SHIPMENT_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    // Optional: handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorBody.put("message", ex.getMessage());
        errorBody.put("code", "INTERNAL_ERROR");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
