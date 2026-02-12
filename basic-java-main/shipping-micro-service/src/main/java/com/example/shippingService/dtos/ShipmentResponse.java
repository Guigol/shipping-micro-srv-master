package com.example.shippingService.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@Data
public class ShipmentResponse {

    private String shipmentId;
    private Long userId;

    private String status;

    private ContactInfo sender;
    private ContactInfo receiver;

    private String trackingNumber;
    private String carrier;

    @JsonAlias({"weight_kg", "weight"})
    private Double weight;

    private List<Map<String, Object>> statusHistory;
    private Map<String, Map<String, Object>> files;

    private Instant createdAt;
    private Instant updatedAt;

    public ShipmentResponse() {}
}
