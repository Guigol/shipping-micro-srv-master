package com.example.shippingService.repositories;

import com.example.shippingService.entities.Shipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ShipmentRepository extends MongoRepository<Shipment, String> {
    Optional<Shipment> findByShipmentId(String shipmentId);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
