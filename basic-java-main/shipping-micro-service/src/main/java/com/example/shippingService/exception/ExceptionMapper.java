package com.example.shippingService.exception;

import org.springframework.http.HttpStatus;

public final class ExceptionMapper {

    private ExceptionMapper() {}

    public static ErrorDescriptor map(Exception ex) {

        if (ex instanceof ShipmentNotFoundException) {
            return new ErrorDescriptor(
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase(),
                    ex.getMessage(),
                    "SHIPMENT_NOT_FOUND"
            );
        }

        return new ErrorDescriptor(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                "INTERNAL_ERROR"
        );
    }
}
