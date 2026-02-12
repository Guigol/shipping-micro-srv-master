package com.example.shippingService.exception;

public record ErrorDescriptor(
        int httpStatus,
        String error,
        String message,
        String code
) {}

