package com.example.gateway.service;

import com.example.gateway.dtos.*;
import com.example.gateway.exception.NatsConnectionException;
import com.example.gateway.exception.NatsTimeoutException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NatsGatewayService {

    private final Connection connection;
    private final ObjectMapper objectMapper;

    @Value("${nats.timeout:5000}")
    private int timeout;

    private static final String SUBJECT_CREATE_USER = "user.create";
    private static final String SUBJECT_GET_USER = "user.get";
    private static final String SUBJECT_GET_ALL_USERS = "user.getAll";
    private static final String SUBJECT_UPDATE_USER = "user.update";
    private static final String SUBJECT_DELETE_USER = "user.delete";
    private static final String SUBJECT_NOTIFY_USER = "user.notification";

    private static final String SUBJECT_SHIPPING_CREATE = "shipping.create";
    private static final String SUBJECT_GET_ALL_SHIPMENTS = "shipping.getAll";
    private static final String SUBJECT_GET_SHIPMENT_BY_ID = "shipping.getByShipmentId";
    private static final String SUBJECT_UPDATE_SHIPPING = "shipping.update";
    private static final String SUBJECT_DELETE_SHIPPING = "shipping.delete";

    private static final String SUBJECT_TRACKING_GET = "shipping.tracking.get";
    private static final String SUBJECT_TRACKING_STATUS_ADD = "shipping.tracking.add";

    private static final String SUBJECT_USER_LOGIN = "user.login";

    /**
     * Generic method to send a NATS request to a specific subject.
     * For shipping requests, the payload is sent **as-is**, no wrapper.
     */
    public NatsResponse sendRequest(String subject, Object payload) {
        log.debug("Sending generic NATS request to subject '{}': {}", subject, payload);
        try {
            checkConnection();

            String requestJson;

            // login has special handling
            if (SUBJECT_USER_LOGIN.equals(subject)) {
                requestJson = objectMapper.writeValueAsString(payload);
                log.info("FINAL JSON SENT TO NATS [user.login] → {}", requestJson);

            } else if (SUBJECT_SHIPPING_CREATE.equals(subject)
                    || SUBJECT_GET_ALL_SHIPMENTS.equals(subject)
                    || SUBJECT_GET_SHIPMENT_BY_ID.equals(subject)
                    || SUBJECT_UPDATE_SHIPPING.equals(subject)
                    || SUBJECT_DELETE_SHIPPING.equals(subject)) {

                // shipping: send payload **as-is**, do not wrap userId
                requestJson = objectMapper.writeValueAsString(payload);
               // log.info("FINAL JSON SENT TO NATS [{}] → {}", subject, requestJson);

            } else {
                // wrapper for other services (user, notifications)
                Map<String, Object> wrapper = new HashMap<>();
                wrapper.put("data", payload);
                requestJson = objectMapper.writeValueAsString(wrapper);
                //log.info("FINAL JSON SENT TO NATS [{}] → {}", subject, requestJson);
            }

            Message reply = connection.request(
                    subject,
                    requestJson.getBytes(StandardCharsets.UTF_8),
                    Duration.ofMillis(timeout)
            );

            return deserializeResponse(reply);

        } catch (TimeoutException e) {
            throw new NatsTimeoutException("NATS timeout on subject " + subject, e);
        } catch (Exception e) {
            throw new NatsConnectionException("Failed NATS communication on subject " + subject, e);
        }
    }

    // ==== USER OPERATIONS ====

    public NatsResponse createUser(CreateUserRequest request) {
        return sendRequest(SUBJECT_CREATE_USER, request);
    }

    public NatsResponse getUserById(Long id) {
        Map<String, Object> request = new HashMap<>();
        request.put("id", id);
        return sendRequest(SUBJECT_GET_USER, request);
    }

    public NatsResponse getAllUsers() {
        return sendRequest(SUBJECT_GET_ALL_USERS, new HashMap<>());
    }

    public NatsResponse updateUser(Long id, UpdateUserRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("id", id);
        requestMap.put("name", request.getName());
        requestMap.put("email", request.getEmail());
        requestMap.put("address", request.getAddress());
        requestMap.put("role", request.getRole());

        return sendRequest(SUBJECT_UPDATE_USER, requestMap);
    }

    public NatsResponse deleteUser(Long id) {
        Map<String, Object> request = new HashMap<>();
        request.put("id", id);
        return sendRequest(SUBJECT_DELETE_USER, request);
    }

    public void notifyUser(Long id, NotificationRequest request) {
        try {
            checkConnection();
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", id);
            payload.put("message", request.getMessage());
            payload.put("timestamp", java.time.LocalDateTime.now().toString());

            String json = objectMapper.writeValueAsString(payload);
            connection.publish(SUBJECT_NOTIFY_USER, json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new NatsConnectionException("Failed to send notification", e);
        }
    }

    public NatsResponse login(LoginRequest request) {
        return sendRequest(SUBJECT_USER_LOGIN, request);
    }

    // ==== SHIPPING OPERATIONS ====

    public NatsResponse getAllShipments() {
        return sendRequest(SUBJECT_GET_ALL_SHIPMENTS, new HashMap<>());
    }

    public NatsResponse createShipping(ShipmentRequest request) {
        return sendRequest(SUBJECT_SHIPPING_CREATE, request);
    }

    public NatsResponse getShipmentByShipmentId(String shipmentId) {
        Map<String, Object> request = new HashMap<>();
        request.put("shipmentId", shipmentId);
        return sendRequest(SUBJECT_GET_SHIPMENT_BY_ID, request);
    }

    public NatsResponse updateShipment(ShipmentRequest request) {
        return sendRequest(SUBJECT_UPDATE_SHIPPING, request);
    }

    public NatsResponse deleteShipment(String shipmentId) {
        Map<String, Object> request = new HashMap<>();
        request.put("shipmentId", shipmentId);
        return sendRequest(SUBJECT_DELETE_SHIPPING, request);
    }

    public NatsResponse getTrackingInfo(String shipmentId) {
        Map<String, Object> request = new HashMap<>();
        request.put("shipmentId", shipmentId);
        return sendRequest(SUBJECT_TRACKING_GET, request);
    }

    // ==== INTERNAL ====

    private void checkConnection() {
        if (connection == null || connection.getStatus() != Connection.Status.CONNECTED) {
            throw new NatsConnectionException("NATS server is not available");
        }
    }

    private NatsResponse deserializeResponse(Message message) throws Exception {
        if (message == null || message.getData() == null) {
            throw new NatsConnectionException("Empty response from NATS");
        }
        String json = new String(message.getData(), StandardCharsets.UTF_8);
        log.debug("Raw NATS response: {}", json);
        return objectMapper.readValue(json, NatsResponse.class);
    }
}
