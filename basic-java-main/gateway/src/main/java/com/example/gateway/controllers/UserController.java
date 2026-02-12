package com.example.gateway.controllers;

import com.example.gateway.dtos.CreateUserRequest;
import com.example.gateway.dtos.NatsResponse;
import com.example.gateway.dtos.NotificationRequest;
import com.example.gateway.dtos.UpdateUserRequest;
import com.example.gateway.dtos.UserDto;
import com.example.gateway.service.NatsGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final NatsGatewayService natsGatewayService;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/users - Create a new user
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /api/users - Creating user: {}", request.getEmail());
        
        NatsResponse response = natsGatewayService.createUser(request);
        
        if (response.isSuccess()) {
            UserDto user = objectMapper.convertValue(response.getData(), UserDto.class);
            log.info("User created successfully: id={}", user.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } else {
            log.warn("Failed to create user: {}", response.getError().getMessage());
            return handleErrorResponse(response);
        }
    }

    /**
     * GET /api/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user", id);

        NatsResponse response = natsGatewayService.getUserById(id);

        if (response.isSuccess()) {
            UserDto user = objectMapper.convertValue(response.getData(), UserDto.class);
            log.info("User fetched successfully: id={}", user.getUserId());
            return ResponseEntity.ok(user);
        } else {
            log.warn("Failed to fetch user id={}: {}", id, response.getError().getMessage());
            return handleErrorResponse(response);
        }
    }

    /**
     * GET /api/users - Get all users
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        log.info("GET /api/users - Fetching all users");

        NatsResponse response = natsGatewayService.getAllUsers();

        if (response.isSuccess()) {

            List<UserDto> users = objectMapper.convertValue(
                    response.getData(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserDto.class)
            );
            log.info("Fetched {} users successfully", users.size());
            return ResponseEntity.ok(users);
        } else {
            log.warn("Failed to fetch all users: {}", response.getError().getMessage());
            return handleErrorResponse(response);
        }
    }

    /**
     * PUT /api/users/{id} - Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("PUT /api/users/{} - Updating user", id);
        
        NatsResponse response = natsGatewayService.updateUser(id, request);
        
        if (response.isSuccess()) {
            UserDto user = objectMapper.convertValue(response.getData(), UserDto.class);
            log.info("User updated successfully: id={}", user.getUserId());
            return ResponseEntity.ok(user);
        } else {
            log.warn("Failed to update user id={}: {}", id, response.getError().getMessage());
            return handleErrorResponse(response);
        }
    }

    /**
     * DELETE /api/users/{id} - Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        
        NatsResponse response = natsGatewayService.deleteUser(id);
        
        if (response.isSuccess()) {
            log.info("User deleted successfully: id={}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Failed to delete user id={}: {}", id, response.getError().getMessage());
            return handleErrorResponse(response);
        }
    }

    /**
     * POST /api/users/{id}/notify - Send notification (async)
     */
    @PostMapping("/{id}/notify")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> notifyUser(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request) {
        log.info("POST /api/users/{}/notify - Sending notification", id);
        
        natsGatewayService.notifyUser(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ACCEPTED");
        response.put("message", "Notification sent asynchronously");
        response.put("userId", id);
        response.put("timestamp", LocalDateTime.now());
        
        log.info("Notification accepted for user id={}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Map error codes from NatsResponse to appropriate HTTP status codes
     */
    private ResponseEntity<?> handleErrorResponse(NatsResponse natsResponse) {
        String errorCode = natsResponse.getError().getCode();
        String errorMessage = natsResponse.getError().getMessage();
        LocalDateTime timestamp = natsResponse.getError().getTimestamp();
        
        HttpStatus httpStatus;
        
        switch (errorCode) {
            case "USER_NOT_FOUND":
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            case "DUPLICATE_EMAIL":
                httpStatus = HttpStatus.CONFLICT;
                break;
            case "VALIDATION_ERROR":
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case "NATS_TIMEOUT":
                httpStatus = HttpStatus.GATEWAY_TIMEOUT;
                break;
            case "NATS_ERROR":
                httpStatus = HttpStatus.BAD_GATEWAY;
                break;
            default:
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
        }
        
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", timestamp != null ? timestamp : LocalDateTime.now());
        errorBody.put("status", httpStatus.value());
        errorBody.put("error", httpStatus.getReasonPhrase());
        errorBody.put("message", errorMessage);
        errorBody.put("code", errorCode);
        
        return ResponseEntity.status(httpStatus).body(errorBody);
    }
}