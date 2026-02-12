package com.example.shippingService.mappers;

import com.example.shippingService.dtos.ContactInfo;
import com.example.shippingService.dtos.ShipmentResponse;
import com.example.shippingService.entities.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) return null;

        ShipmentResponse res = new ShipmentResponse();
        res.setShipmentId(shipment.getShipmentId());
        res.setUserId(shipment.getUserId());

        res.setSender(toContactInfo(shipment.getSender()));
        res.setReceiver(toContactInfo(shipment.getReceiver()));

        res.setTrackingNumber(shipment.getTrackingNumber());
        res.setCarrier(shipment.getCarrier());
        res.setWeight(shipment.getWeight_kg());
        res.setStatus(shipment.getCurrentStatus());

        res.setStatusHistory(shipment.getStatusHistory());
        res.setFiles(shipment.getFiles());
        res.setCreatedAt(shipment.getCreatedAt());
        res.setUpdatedAt(shipment.getUpdatedAt());

        return res;
    }

    private ContactInfo toContactInfo(ContactInfo src) {
        return src == null ? null : new ContactInfo(src.getName(), src.getAddress());
    }
}
