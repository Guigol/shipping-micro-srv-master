package com.example.shippingService.exception;

public final class ErrorMessages {

    private ErrorMessages() {}

    // ----------- Shipment -----------
    public static final String SHIPMENT_NOT_FOUND =
            "No shipment found with shipmentId: %s";

    public static final String INVALID_SHIPMENT_ID =
            "shipmentId is missing in NATS message";

    public static final String INVALID_GET_BY_ID_PAYLOAD =
            "Invalid payload for shipping.getByShipmentId: %s";

    public static final String MISSING_SHIPMENT_ID_IN_PAYLOAD =
            "shipmentId missing in shipping.getByShipmentId payload: %s";


       // ----------- Tracking -----------
    public static final String INVALID_TRACKING_ADD_PAYLOAD =
            "Invalid payload structure for shipping.tracking.add: %s";

    public static final String SHIPMENT_NOT_FOUND_BY_TRACKING =
            "No shipment found with trackingNumber: %s";

    public static final String INVALID_USER_ID_TYPE =
            "Invalid userId type: %s";

    public static final String UNSUPPORTED_TIMESTAMP_FORMAT =
            "Unsupported timestamp format: %s";


}

