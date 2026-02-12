package com.example.gateway.dtos;

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

    /** The shipment ID */
    private String shipmentId;

    /** The current shipment status (ex: CREATED, IN_TRANSIT, DELIVERED) */
    private String currentStatus;

    /** List of tracking history entries */
    private List<StatusEntry> history;

    private Long userId;

    /**
     * Convenience constructor for service layer.
     */
    public TrackingResponse(String shipmentId, String currentStatus, List<StatusEntry> history, Long userId) {
        this.shipmentId = shipmentId;
        this.currentStatus = currentStatus;
        this.history = history;
        this.userId = userId;
    }

    /**
     * Inner class representing a single status update in the tracking history.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatusEntry {

        private String status;
        private Instant timestamp;
        private String location;
        private String note;
        private Long userId;
    }
}
