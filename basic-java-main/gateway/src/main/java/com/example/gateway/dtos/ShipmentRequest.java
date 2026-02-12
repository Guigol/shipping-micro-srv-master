package com.example.gateway.dtos;

import com.example.gateway.exception.InvalidShipmentException;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentRequest {
    //private String id;

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


    // Jackson's map metadata from JSON
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

    public void validate() {
        // Weight check
        if (weight == null || weight <= 0) {
            throw new InvalidShipmentException(shipmentId, "Weight cannot be null or zero");
        }

        // Carrier check
        if (carrier == null) {
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
