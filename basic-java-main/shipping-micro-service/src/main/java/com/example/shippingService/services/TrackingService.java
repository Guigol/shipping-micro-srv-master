package com.example.shippingService.services;

import com.example.shippingService.dtos.AddTrackingStatusRequest;
import com.example.shippingService.dtos.TrackingResponse;
import com.example.shippingService.dtos.TrackingResponse.StatusEntry;
import com.example.shippingService.entities.Shipment;
import com.example.shippingService.exception.ShipmentNotFoundException;
import com.example.shippingService.exception.ErrorMessages;
import com.example.shippingService.repositories.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ShipmentRepository shipmentRepository;

    /**
     * Retrieve tracking info by trackingNumber ONLY
     */
    @Cacheable(value = "tracking", key = "#trackingNumber")
    public TrackingResponse getTrackingInfoByTrackingNumber(String trackingNumber) {
        log.debug("🔍 Fetching tracking info for trackingNumber: {}", trackingNumber);

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(
                        String.format(
                                ErrorMessages.SHIPMENT_NOT_FOUND_BY_TRACKING,
                                trackingNumber
                        )
                ));

        List<Map<String, Object>> trackingHistory = shipment.getTrackingHistory();
        if (trackingHistory == null) trackingHistory = new ArrayList<>();

        List<StatusEntry> statusEntries = trackingHistory.stream()
                .map(entry -> new StatusEntry(
                        (String) entry.get("status"),
                        toLong(entry.get("userId")),
                        convertToInstant(entry.get("timestamp")),
                        (String) entry.get("location"),
                        (String) entry.get("note")
                ))
                .toList();

        return new TrackingResponse(
                shipment.getShipmentId(),
                shipment.getCurrentStatus(),
                statusEntries,
                shipment.getUserId()
        );
    }

    /**
     * Add tracking status using trackingNumber ONLY
     */
    @Caching(put = { @CachePut(value = "tracking", key = "#trackingNumber")})
    public TrackingResponse addTrackingStatusByTrackingNumber(
            String trackingNumber,
            AddTrackingStatusRequest request,
            String userIdFromRequest) {

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(
                        String.format(
                                ErrorMessages.SHIPMENT_NOT_FOUND_BY_TRACKING,
                                trackingNumber
                        )
                ));

        Instant timestamp = request.getTimestamp() != null
                ? convertToInstant(request.getTimestamp())
                : Instant.now();

        Map<String, Object> entry = new HashMap<>();
        entry.put("status", request.getStatus());
        entry.put("location", request.getLocation());
        entry.put("note", request.getNote());
        entry.put("timestamp", timestamp);

        // UserId can be String or Long -> store as String if necessary
        entry.put("userId",
                userIdFromRequest != null
                        ? userIdFromRequest
                        : String.valueOf(shipment.getUserId())
        );

        if (shipment.getTrackingHistory() == null) {
            shipment.setTrackingHistory(new ArrayList<>());
        }
        shipment.getTrackingHistory().add(entry);

        shipment.setCurrentStatus(request.getStatus());
        shipment.setUpdatedAt(Instant.now());
        shipmentRepository.save(shipment);

        List<StatusEntry> history = shipment.getTrackingHistory().stream()
                .map(e -> new StatusEntry(
                        (String) e.get("status"),
                        toLong(e.get("userId")),
                        convertToInstant(e.get("timestamp")),
                        (String) e.get("location"),
                        (String) e.get("note")
                ))
                .toList();

        return new TrackingResponse(
                shipment.getShipmentId(),
                shipment.getCurrentStatus(),
                history,
                shipment.getUserId() != null
                        ? shipment.getUserId()
                        : toLong(userIdFromRequest)
        );
    }

    /**
     * Convert any stored userId type to Long safely.
     */
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof String s && !s.isEmpty()) return Long.parseLong(s);
        throw new IllegalArgumentException(
                String.format(
                        ErrorMessages.INVALID_USER_ID_TYPE,
                        value
                )
        );
    }

    private Instant convertToInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof java.util.Date date) return date.toInstant();
        if (value instanceof Long epochMillis) return Instant.ofEpochMilli(epochMillis);
        if (value instanceof String str) return Instant.parse(str);
        throw new IllegalArgumentException(
                String.format(
                        ErrorMessages.UNSUPPORTED_TIMESTAMP_FORMAT,
                        value
                )
        );
    }
}