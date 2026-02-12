package com.example.shippingService.entities;

import com.example.shippingService.dtos.ContactInfo;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shipments")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shipment {


    //private String id; // MongoDB
    @Id
    private String shipmentId; // PUBLIC -> ex: "SHIP-2025-0001"

    private Long userId;

    private ContactInfo sender;   // { id, name, address }
    private ContactInfo receiver; // { id, name, address }

    private String carrier;               // "La Poste"

    @Indexed(unique = true)
    private String trackingNumber;        // "LP123456789FR"

    private String currentStatus;

    @JsonAlias({"weightkg", "weight"})
    private Double weight_kg;

    private List<Map<String, Object>> statusHistory;

    private Map<String, Object> metadata; //(weight)

    private Map<String, Map<String, Object>> files;// URLs proof / label

    private Instant createdAt;
    private Instant updatedAt;

    public void setUserId(Object userId) {
        if (userId == null || "null".equals(userId.toString())) {
            this.userId = null;
        } else {
            this.userId = Long.valueOf(userId.toString());
        }
    }


    /**
     * List of tracking events (status updates over time).
     * Each entry contains: status, timestamp, location, note...
     */
    @Builder.Default
    private List<Map<String, Object>> trackingHistory = new ArrayList<>();

    /**
     * Adds a new tracking status entry.
     */
    public void addTrackingStatus(String status, String location, String note) {

        Map<String, Object> entry = new HashMap<>();
        entry.put("status", status);
        entry.put("timestamp", Instant.now());
        if (location != null) entry.put("location", location);
        if (note != null) entry.put("note", note);

        this.trackingHistory.add(entry);

        // ✅ Update main shipment status
        this.currentStatus = status;

        this.updatedAt = Instant.now();
    }
}
