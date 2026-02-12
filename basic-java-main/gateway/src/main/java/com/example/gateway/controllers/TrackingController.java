package com.example.gateway.controllers;

import com.example.gateway.dtos.AddTrackingStatusRequest;
import com.example.gateway.dtos.NatsResponse;
import com.example.gateway.service.NatsGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final NatsGatewayService natsGatewayService;

    // GET Tracking by trackingNumber
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<NatsResponse> getTrackingByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(
                natsGatewayService.sendRequest(
                        "shipping.tracking.get",
                        Map.of("trackingNumber", trackingNumber)
                )
        );
    }



    // Add Tracking status via NATS
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{trackingNumber}/add")
    public ResponseEntity<NatsResponse> addTrackingStatus(
            @PathVariable String trackingNumber,
            @RequestBody AddTrackingStatusRequest request
    ) {
        return ResponseEntity.ok(
                natsGatewayService.sendRequest(
                        "shipping.tracking.add",
                        Map.of(
                                "trackingNumber", trackingNumber,
                                "status", request.getStatus(),
                                "location", request.getLocation(),
                                "note", request.getNote()
                        )
                )
        );
    }

    /**
     * Map error codes from NatsResponse to appropriate HTTP status codes
     */
    private ResponseEntity<?> handleErrorResponse(NatsResponse natsResponse) {
        String errorCode = natsResponse.getError().getCode();
        String errorMessage = natsResponse.getError().getMessage();
        LocalDateTime timestamp = natsResponse.getError().getTimestamp();

        HttpStatus httpStatus = switch (errorCode) {
            case "TRACKING_NUMBER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "NATS_TIMEOUT" -> HttpStatus.GATEWAY_TIMEOUT;
            case "NATS_ERROR" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("timestamp", timestamp != null ? timestamp : LocalDateTime.now());
            errorBody.put("status", httpStatus.value());
            errorBody.put("error", httpStatus.getReasonPhrase());
            errorBody.put("message", errorMessage);
            errorBody.put("code", errorCode);

        return ResponseEntity.status(httpStatus).body(errorBody);
    }
}

