package com.example.shippingService.listener;

import com.example.shippingService.dtos.*;
import com.example.shippingService.entities.Shipment;
import com.example.shippingService.exception.ErrorMessages;
import com.example.shippingService.mappers.ShipmentMapper; // <-- ajout
import com.example.shippingService.services.FileStorageService;
import com.example.shippingService.services.ShipmentService;
import com.example.shippingService.services.TrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingNatsListener {

    private final Connection natsConnection;
    private final ShipmentService shipmentService;
    private final TrackingService trackingService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final ShipmentMapper shipmentMapper;


    private Map<String, Object> responseToMap(ShipmentResponse shipment) {
        return objectMapper.convertValue(shipment, Map.class);
    }

    @PostConstruct
    public void init() {

                Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
            try {
                String subject = msg.getSubject();
                String requestJson = new String(msg.getData(), StandardCharsets.UTF_8);
                log.info("Received NATS message on '{}': {}", subject, requestJson);

                Map<String, Object> responseMap = new HashMap<>();

                switch (subject) {

                    case "shipping.create" -> {
                        Map<String, Object> requestMap = objectMapper.readValue(msg.getData(), Map.class);
                        ShipmentRequest shipmentRequest = objectMapper.convertValue(requestMap, ShipmentRequest.class);
                        shipmentRequest.validate();

                        ShipmentResponse shipment = shipmentService.createShipment(shipmentRequest);
                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "Shipment successfully created");
                        responseMap.put("data", responseToMap(shipment));
                    }

                    case "shipping.getAll" -> {
                        List<ShipmentResponse> shipments =
                                shipmentService.getAllShipments();

                        List<Map<String, Object>> shipmentsData = shipments.stream()
                                .map(this::responseToMap)
                                .toList();

                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "All shipments retrieved successfully");
                        responseMap.put("data", shipmentsData);
                    }

                    case "shipping.getByShipmentId" -> {
                        Map<String, Object> request = objectMapper.readValue(requestJson, Map.class);

                        Object dataObj = request.get("data");
                        if (dataObj == null) {
                            dataObj = request;
                        }

                        if (!(dataObj instanceof Map)) {
                            throw new IllegalArgumentException(
                                    String.format(
                                            ErrorMessages.INVALID_GET_BY_ID_PAYLOAD,
                                            request
                                    )
                            );
                        }

                        Object shipmentIdObj =
                                ((Map<String, Object>) dataObj).get("shipmentId");

                        if (shipmentIdObj == null) {
                            throw new IllegalArgumentException(
                                    String.format(
                                            ErrorMessages.MISSING_SHIPMENT_ID_IN_PAYLOAD,
                                            request
                                    )
                            );
                        }

                        String shipmentId = shipmentIdObj.toString();

                        ShipmentResponse shipment =
                                shipmentService.getByShipmentId(shipmentId);

                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("data", responseToMap(shipment));
                    }

                    case "shipping.update" -> {
                        ShipmentRequest shipmentRequest = objectMapper.readValue(requestJson, ShipmentRequest.class);
                        ShipmentResponse updated = shipmentService.updateShipment(shipmentRequest);
                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "Shipment successfully updated");
                        responseMap.put("data", responseToMap(updated));
                    }

                    case "shipping.delete" -> {
                        Map<String, Object> requestMap =
                                objectMapper.readValue(requestJson, Map.class);
                        Object shipmentIdObj = requestMap.get("shipmentId");
                        if (shipmentIdObj == null) {
                            throw new IllegalArgumentException(
                                    ErrorMessages.INVALID_SHIPMENT_ID
                            );
                        }

                        String shipmentId = shipmentIdObj.toString();

                        shipmentService.deleteShipment(shipmentId);
                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "Shipment deleted successfully (ID: " + shipmentId + ")");
                        responseMap.put("data", null);
                    }

                    case "shipping.tracking.get" -> {
                        Map<String, Object> requestMap = objectMapper.readValue(requestJson, Map.class);
                        String trackingNumber = requestMap.get("trackingNumber") != null
                                ? requestMap.get("trackingNumber").toString()
                                : ((Map<String, Object>) requestMap.get("data")).get("trackingNumber").toString();
                        TrackingResponse tr = trackingService.getTrackingInfoByTrackingNumber(trackingNumber);
                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "Tracking info retrieved successfully");
                        responseMap.put("data", tr);
                    }

                    case "shipping.tracking.add" -> {

                        Map<String, Object> request =
                                objectMapper.readValue(requestJson, Map.class);

                        /* =========================
                           Extract userId
                           ========================= */
                        Long userId = null;
                        Object userIdObj = request.get("userId");

                        if (userIdObj != null) {
                            String userIdStr = userIdObj.toString();
                            if (!userIdStr.isBlank() && !"null".equalsIgnoreCase(userIdStr)) {
                                try {
                                    userId = Long.parseLong(userIdStr);
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid userId received in tracking.add: {}", userIdStr);
                                }
                            }
                        }

                        /* =========================
                           Extract data
                           ========================= */
                        Object dataObj = request.get("data");

                        if (dataObj == null) {
                            // fallback : payload plat
                            dataObj = request;
                        }

                        if (!(dataObj instanceof Map)) {
                            throw new IllegalArgumentException(
                                    String.format(
                                            ErrorMessages.INVALID_TRACKING_ADD_PAYLOAD,
                                            request
                                    )
                            );
                        }

                        AddTrackingStatusRequest addReq =
                                objectMapper.convertValue(dataObj, AddTrackingStatusRequest.class);

                        addReq.setUserId(userId);

                        /* =========================
                           service call
                           ========================= */
                        TrackingResponse tr =
                                trackingService.addTrackingStatusByTrackingNumber(
                                        addReq.getTrackingNumber(),
                                        addReq,
                                        userId != null ? userId.toString() : null
                                );

                        /* =========================
                           Response
                           ========================= */
                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "Tracking status added successfully");
                        responseMap.put("data", tr);
                    }

                    case "shipping.file.upload" -> {
                        Map<String, Object> body = objectMapper.readValue(requestJson, Map.class);
                        ProofUploadRequest req = objectMapper.convertValue(body, ProofUploadRequest.class);
                        Map<String, Object> saved = fileStorageService.storeFile(req);

                        responseMap.put("success", true);
                        responseMap.put("status", "success");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", "File stored successfully");
                        responseMap.put("data", saved);
                    }

                    case "shipping.file.get" -> {
                        Map<String, Object> body = objectMapper.readValue(requestJson, Map.class);
                        String shipmentId = body.get("shipmentId").toString();
                        String type = body.get("type").toString();
                        Map<String, Object> fileData = fileStorageService.getProof(shipmentId, type);

                        responseMap.put("success", fileData != null);
                        responseMap.put("status", fileData != null ? "success" : "error");
                        responseMap.put("source", "Shipping-service");
                        responseMap.put("message", fileData != null ? "File retrieved successfully" : "No file found for shipment: " + shipmentId);
                        responseMap.put("data", fileData);
                    }
                }

                String responseJson = objectMapper.writeValueAsString(responseMap);
                natsConnection.publish(msg.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));

            } catch (Exception e) {
                try {
                    Map<String, Object> errorResponse =
                            NatsErrorResponseFactory.build(e);

                    String errorJson = objectMapper.writeValueAsString(errorResponse);
                    natsConnection.publish(
                            msg.getReplyTo(),
                            errorJson.getBytes(StandardCharsets.UTF_8)
                    );
                } catch (Exception ignored) {}
            }
      });

        dispatcher.subscribe("shipping.create");
        dispatcher.subscribe("shipping.getAll");
        dispatcher.subscribe("shipping.getByShipmentId");
        dispatcher.subscribe("shipping.update");
        dispatcher.subscribe("shipping.delete");
        dispatcher.subscribe("shipping.tracking.get");
        dispatcher.subscribe("shipping.tracking.add");
        dispatcher.subscribe("shipping.file.upload");
        dispatcher.subscribe("shipping.file.get");

     }
}
