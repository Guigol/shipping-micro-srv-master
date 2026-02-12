package com.example.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidShipmentException extends RuntimeException {

    private final String shipmentId;

    public InvalidShipmentException(String message) {
        super(message);
        this.shipmentId = null;
    }

    public InvalidShipmentException(String shipmentId, String message) {
        super(message);
        this.shipmentId = shipmentId;
    }

    public String getShipmentId() {
        return shipmentId;
    }
}
