package com.example.shippingService.services;

import com.example.shippingService.dtos.ContactInfo;
import com.example.shippingService.dtos.ShipmentRequest;
import com.example.shippingService.dtos.ShipmentResponse;
import com.example.shippingService.entities.Shipment;
import com.example.shippingService.exception.ShipmentNotFoundException;
import com.example.shippingService.mappers.ShipmentMapper;
import com.example.shippingService.repositories.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    /* ---------------------------------------------------------
        CREATE SHIPMENT
    --------------------------------------------------------- */
    @CachePut(value = "shipments", key = "#result.shipmentId")
    public ShipmentResponse createShipment(ShipmentRequest request) {
        request.validate();

        ContactInfo sender = request.getSender() != null
                ? new ContactInfo(request.getSender().getName(), request.getSender().getAddress())
                : null;

        ContactInfo receiver = request.getReceiver() != null
                ? new ContactInfo(request.getReceiver().getName(), request.getReceiver().getAddress())
                : null;

        Shipment shipment = Shipment.builder()
                .shipmentId("SHIP-" + UUID.randomUUID().toString().substring(0, 16))
                .userId(request.getUserId())
                .sender(sender)
                .receiver(receiver)
                .carrier(request.getCarrier())
                .trackingNumber("LP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .currentStatus("CREATED")
                .weight_kg(request.getWeight())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        shipmentRepository.save(shipment);

        return shipmentMapper.toResponse(shipment);
    }

    /* ---------------------------------------------------------
        GET ALL SHIPMENTS
    --------------------------------------------------------- */

    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    /* ---------------------------------------------------------
        GET BY SHIPMENT ID
    --------------------------------------------------------- */
    @Cacheable(value = "shipments", key = "#shipmentId")
    public ShipmentResponse getByShipmentId(String shipmentId) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        return shipmentMapper.toResponse(shipment);
    }

    /* ---------------------------------------------------------
        UPDATE SHIPMENT
    --------------------------------------------------------- */
    @CachePut(value = "shipments", key = "#result.shipmentId")
    public ShipmentResponse updateShipment(ShipmentRequest request) {
        request.validate();

        Shipment shipment = shipmentRepository.findByShipmentId(request.getShipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException((request.getShipmentId())));

        if (request.getWeight() != null) shipment.setWeight_kg(request.getWeight());
        if (request.getCarrier() != null) shipment.setCarrier(request.getCarrier());
        if (request.getTrackingNumber() != null) shipment.setTrackingNumber(request.getTrackingNumber());

        if (request.getSender() != null) {
            shipment.setSender(new ContactInfo(
                    request.getSender().getName(),
                    request.getSender().getAddress()
            ));
        }

        if (request.getReceiver() != null) {
            shipment.setReceiver(new ContactInfo(
                    request.getReceiver().getName(),
                    request.getReceiver().getAddress()
            ));
        }

        if (request.getUserId() != null) shipment.setUserId(request.getUserId());

        shipment.setUpdatedAt(Instant.now());

        shipmentRepository.save(shipment);

        return shipmentMapper.toResponse(shipment);
    }

    /* ---------------------------------------------------------
        DELETE SHIPMENT
    --------------------------------------------------------- */
    @CacheEvict(value = "shipments", key = "#shipmentId")
    public void deleteShipment(String shipmentId) {
        Shipment shipment = shipmentRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        shipmentRepository.delete(shipment);
    }

}
