package com.example.shippingService.exception;

/**
 * Exception thrown when a shipment with a given ID is not found.
 */
public class ShipmentNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE =
            "No shipment found with shipmentId: %s";

    public ShipmentNotFoundException(String shipmentId) {
        super(String.format(DEFAULT_MESSAGE, shipmentId));
    }

    public ShipmentNotFoundException(String shipmentId, Throwable cause) {
        super(String.format(DEFAULT_MESSAGE, shipmentId), cause);
    }

    // Keep generic constructors if used elsewhere
    public ShipmentNotFoundException() {
        super();
    }

     public ShipmentNotFoundException(Throwable cause) {
        super(cause);
    }
}

