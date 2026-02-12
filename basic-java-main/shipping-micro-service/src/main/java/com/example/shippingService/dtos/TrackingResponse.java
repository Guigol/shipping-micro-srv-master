package com.example.shippingService.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO used to return shipment tracking details.
 */
@Data
@Builder
@NoArgsConstructor
public class TrackingResponse {

    private String shipmentId;
    private String currentStatus;
    private List<StatusEntry> history;
    private Long userId;

    /**
     * Base constructor used inside the class after conversion.
     */
    public TrackingResponse(String shipmentId,
                            String currentStatus,
                            List<StatusEntry> history,
                            Long userId) {
        this.shipmentId = shipmentId;
        this.currentStatus = currentStatus;
        this.history = history;
        this.userId = userId;
    }

    /**
     * Universal constructor accepting ANY userId type (String, Integer, Long, DB generic types).
     */
    public TrackingResponse(String shipmentId,
                            String currentStatus,
                            List<StatusEntry> history,
                            Object userId) {

        this(
                shipmentId,
                currentStatus,
                history,
                convertToLong(userId)
        );
    }

    /**
     * Robust userId conversion for ANY object type.
     */
    private static Long convertToLong(Object value) {
        if (value == null) return null;

        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s) return Long.parseLong(s);

        // If DB gives a weird generic type (Serializable & Comparable ...)
        if (value instanceof Number n) return n.longValue();

        throw new IllegalArgumentException("Unsupported userId type: " + value + " (" + value.getClass() + ")");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatusEntry {
        private String status;
        private Long userId;
        private Instant timestamp;
        private String location;
        private String note;
    }
}
