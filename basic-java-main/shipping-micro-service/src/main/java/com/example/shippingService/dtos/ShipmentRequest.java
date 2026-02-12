package com.example.shippingService.dtos;

import com.example.shippingService.exception.InvalidShipmentException;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentRequest {

    private static final double MAX_WEIGHT_KG = 9.0;

    @JsonAlias({"_id", "shipmentId"})
    private String shipmentId;  // sent by NATS service ("SHIP-2025-0001")

    private Long userId;

    private ContactInfo sender;
    private ContactInfo receiver;
    private String carrier;
    private String trackingNumber;
    private String currentStatus;
    private Object statusHistory;
    private Object files;

    private String createdAt;
    private String updatedAt;

    // Accept both "weight" and "weight_kg" at root level
    @JsonAlias({"weight_kg", "weight"})
    private Double weight;

    // Still support metadata.weight_kg
    @JsonProperty("metadata")
    private void unpackMetadata(Map<String, Object> metadata) {
        if (metadata != null && metadata.get("weight_kg") != null) {
            Object w = metadata.get("weight_kg");
            if (w instanceof Number) {
                this.weight = ((Number) w).doubleValue();
            } else if (w instanceof String) {
                try {
                    this.weight = Double.parseDouble((String) w);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    /**
     * Extra setter specifically annotated to catch "weight_kg" if Jackson
     * for some reason doesn't apply @JsonAlias on the field when converting maps.
     * This is defensive: it ensures weight_kg at root is always recognized.
     */
    @JsonProperty("weight_kg")
    public void setWeightFromWeightKg(Object w) {
        if (w == null) return;
        if (w instanceof Number) {
            this.weight = ((Number) w).doubleValue();
            return;
        }
        if (w instanceof String) {
            try {
                this.weight = Double.parseDouble((String) w);
            } catch (NumberFormatException ignored) {}
        }
    }

    public void validate() {
        // Weight check
        if (weight == null || weight <= 0) {
            throw new InvalidShipmentException(
                    shipmentId,
                    "Weight must be greater than 0"
            );
        }
        if (weight > MAX_WEIGHT_KG) {
            throw new InvalidShipmentException(
                    shipmentId,
                    "Weight cannot exceed " + MAX_WEIGHT_KG + " kg"
            );
        }
        // Carrier check
        if (carrier == null || carrier.isBlank()) {
            throw new InvalidShipmentException(shipmentId, "Carrier must be specified");
        }

        // sender check
        if (sender == null) {
            throw new InvalidShipmentException(shipmentId, "Sender cannot be null");
        }
        if (sender.getName() == null || sender.getName().isBlank()) {
            throw new InvalidShipmentException(shipmentId, "Sender name cannot be null or blank");
        }
        if (sender.getAddress() == null || sender.getAddress().isBlank()) {
            throw new InvalidShipmentException(shipmentId, "Sender address cannot be null or blank");
        }

        // receiver check
        if (receiver == null) {
            throw new InvalidShipmentException(shipmentId, "Receiver cannot be null");
        }
        if (receiver.getName() == null || receiver.getName().isBlank()) {
            throw new InvalidShipmentException(shipmentId, "Receiver name cannot be null or blank");
        }
        if (receiver.getAddress() == null || receiver.getAddress().isBlank()) {
            throw new InvalidShipmentException(shipmentId, "Receiver address cannot be null or blank");
        }
    }
}
