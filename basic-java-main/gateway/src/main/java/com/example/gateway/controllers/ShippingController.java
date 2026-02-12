package com.example.gateway.controllers;

import com.example.gateway.dtos.NatsResponse;
import com.example.gateway.dtos.ShipmentRequest;
import com.example.gateway.dtos.ShipmentResponse;
import com.example.gateway.dtos.UserDto;
import com.example.gateway.exception.InvalidShipmentException;
import com.example.gateway.service.NatsGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final NatsGatewayService natsGatewayService;
    private final ObjectMapper objectMapper;

    /* ============================================================
       🔐 USER ID RESOLUTION
       ============================================================ */
    private Long resolveAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDto userDto) {
            return userDto.getUserId();
        }

        // fallback éventuel (selon impl UserDetails)
        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            try {
                return Long.parseLong(springUser.getUsername());
            } catch (NumberFormatException ignored) {}
        }

        return null;
    }

    private void enforceUserId(ShipmentRequest request) {
        if (request.getUserId() == null) {
            Long uid = resolveAuthenticatedUserId();
            if (uid != null) {
                request.setUserId(uid);
                log.info("✅ Injected userId={} from SecurityContext", uid);
            }
        } else {
            log.info("ℹ️ userId={} already present, not injecting", request.getUserId());
        }

        if (request.getUserId() == null) {
            throw new InvalidShipmentException(
                    request.getShipmentId(),
                    "userId is mandatory and could not be resolved"
            );
        }
    }


    /* ============================================================
       📦 GET ALL SHIPMENTS
       ============================================================ */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllShipments() {

        NatsResponse response = natsGatewayService.getAllShipments();

        if (!response.isSuccess()) {
            return handleErrorResponse(response);
        }

        List<ShipmentResponse> shipments = objectMapper.convertValue(
                response.getData(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, ShipmentResponse.class)
        );

        return ResponseEntity.ok(shipments);
    }

    /* ============================================================
       📦 GET SHIPMENT BY ID
       ============================================================ */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{shipmentId}")
    public ResponseEntity<?> getShipmentById(@PathVariable String shipmentId) {

        NatsResponse response =
                natsGatewayService.getShipmentByShipmentId(shipmentId);

        if (!response.isSuccess()) {
            return handleErrorResponse(response);
        }

        ShipmentResponse shipment =
                objectMapper.convertValue(response.getData(), ShipmentResponse.class);

        return ResponseEntity.ok(shipment);
    }

    /* ============================================================
       📦 CREATE SHIPMENT
       ============================================================ */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<NatsResponse> createShipping(
            @RequestBody ShipmentRequest request) {

        enforceUserId(request); // userId always retrieved
        request.validate();

        try {
            return ResponseEntity.ok(
                    natsGatewayService.createShipping(request)
            );
        } catch (InvalidShipmentException e) {
            return ResponseEntity.badRequest().body(
                    new NatsResponse(false, null, null,
                            "Shipping-service", "VALIDATION_ERROR", e.getMessage())
            );
        } catch (Exception e) {
            log.error("❌ Error while creating shipment", e);
            return ResponseEntity.internalServerError().body(
                    new NatsResponse(false, null, null,
                            "Shipping-service", "ERROR", "Erreur interne")
            );
        }
    }

    /* ============================================================
       ✏️ UPDATE SHIPMENT
       ============================================================ */
    @PutMapping("/{shipmentId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<NatsResponse> updateShipment(
            @PathVariable String shipmentId,
            @RequestBody ShipmentRequest request) {

        log.info("✏️ Updating shipment {}", shipmentId);

        // Inject the userId from the session
        enforceUserId(request);

        // Force the shipmentId to avoid errors
        request.setShipmentId(shipmentId);

        // Important : do not wrap in "data"
        NatsResponse response = natsGatewayService.updateShipment(request);

        return ResponseEntity.ok(response);
    }


    /* ============================================================
         DELETE SHIPMENT
       ============================================================ */
    @DeleteMapping("/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NatsResponse> deleteShipping(
            @PathVariable String shipmentId) {

        try {
            return ResponseEntity.ok(
                    natsGatewayService.sendRequest(
                            "shipping.delete",
                            Map.of("shipmentId", shipmentId)
                    )
            );
        } catch (Exception e) {
            log.error("❌ Error while deleting shipment {}", shipmentId, e);
            return ResponseEntity.internalServerError().body(
                    new NatsResponse(false, null, null,
                            "Shipping-service", "ERROR", "Erreur interne")
            );
        }
    }

    /* ============================================================
       🚨 ERROR HANDLING
       ============================================================ */
    private ResponseEntity<?> handleErrorResponse(NatsResponse natsResponse) {

        String code = natsResponse.getStatus(); // ex: "error"
        String message = natsResponse.getMessage();

        LocalDateTime ts = LocalDateTime.now();

        HttpStatus httpStatus = switch (code.toUpperCase()) {
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "NATS_TIMEOUT"     -> HttpStatus.GATEWAY_TIMEOUT;
            case "NATS_ERROR"       -> HttpStatus.BAD_GATEWAY;
            case "ERROR"            -> HttpStatus.INTERNAL_SERVER_ERROR;
            default                  -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ts);
        body.put("status", httpStatus.value());
        body.put("error", httpStatus.getReasonPhrase());
        body.put("message", message != null ? message : "Erreur interne");
        body.put("code", code);

        return ResponseEntity.status(httpStatus).body(body);
    }

}
