package com.example.userservice.listener;

import com.example.userservice.dto.*;
import com.example.userservice.entity.UserRole;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNatsListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Connection natsConnection;

    @PostConstruct
    public void setupListeners() {
        Dispatcher dispatcher = natsConnection.createDispatcher();
        
        // Subscribe to all user-related subjects
        dispatcher.subscribe("user.create", this::handleCreateUser);
        dispatcher.subscribe("user.get", this::handleGetUser);
        dispatcher.subscribe("user.getAll", this::handleGetAllUsers);
        dispatcher.subscribe("user.update", this::handleUpdateUser);
        dispatcher.subscribe("user.delete", this::handleDeleteUser);
        dispatcher.subscribe("user.notification", this::handleNotifyUser);

        // LOGIN
        dispatcher.subscribe("user.login", this::handleLoginUser);
        log.info("NATS listeners initialized for user service");
    }

    // =====================================================================
    // LOGIN USER - Subject: user.login
    // Request: { "email": "x", "password": "y" }
    // Response: NatsResponse with UserDto or error
    // =====================================================================
    private void handleLoginUser(Message message) {
        String json = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.login': {}", json);

        try {
            // Deserialize login request
            LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

            // Authenticate user
            UserDto user = userService.login(request.getEmail(), request.getPassword());

            // Build success response
            NatsResponse response = NatsResponse.success(user);

            String responseJson = objectMapper.writeValueAsString(response);

            // Send reply
            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));
            }

            log.info("Login success for email {}", request.getEmail());

        } catch (Exception e) {
            log.error("Error processing 'user.login': ", e);
            String errorResponse = buildErrorResponse("LOGIN_FAILED", e.getMessage());

            if (message.getReplyTo() != null) {
                try {
                    natsConnection.publish(
                            message.getReplyTo(),
                            errorResponse.getBytes(StandardCharsets.UTF_8)
                    );
                } catch (Exception ex) {
                    log.error("Error sending error response for login", ex);
                }
            }
        }
    }

    /**
     * Create User - Subject: user.create
     * Request: CreateUserRequest
     * Response: NatsResponse with UserDto
     */
    private void handleCreateUser(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.create': {}", messageData);

        try {
            // Deserialize wrapper
            UserCreateWrapper wrapper = objectMapper.readValue(messageData, UserCreateWrapper.class);

            // Extract the real payload
            CreateUserRequest request = wrapper.getData();

            // Process request
            UserDto userDto = userService.createUser(request);

            // Build success response
            NatsResponse response = NatsResponse.success(userDto);
            String responseJson = objectMapper.writeValueAsString(response);

            log.info("Sending response for 'user.create': success");

            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            log.error("Error processing 'user.create': ", e);
            String errorResponse = buildErrorResponse("INTERNAL_ERROR", e.getMessage());

            if (message.getReplyTo() != null) {
                try {
                    natsConnection.publish(message.getReplyTo(), errorResponse.getBytes(StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    log.error("Error sending error response", ex);
                }
            }
        }
    }

    /**
     * Get User by ID - Subject: user.get
     * Request: { "id": Long }
     * Response: NatsResponse with UserDto
     */
    private void handleGetUser(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.get': {}", messageData);

        try {
            Map<String, Object> request = objectMapper.readValue(messageData, Map.class);

            Object idObj = null;
            if (request.containsKey("id")) {
                idObj = request.get("id");
            } else if (request.containsKey("data")) {
                Map<String,Object> dataMap = (Map<String,Object>) request.get("data");
                if (dataMap != null) {
                    idObj = dataMap.get("id");
                }
            }

            if (idObj == null) {
                log.error("Missing 'id' in request: {}", request);
                sendErrorResponse(message, "BAD_REQUEST", "Missing 'id' in request");
                return;
            }

            Long id;
            try {
                id = Long.valueOf(idObj.toString().trim());
            } catch (NumberFormatException e) {
                log.error("Invalid 'id' format in request: {}", idObj);
                sendErrorResponse(message, "BAD_REQUEST", "'id' must be a valid number");
                return;
            }

            // Process request
            UserDto userDto = userService.getUserById(id);

            NatsResponse response = NatsResponse.success(userDto);
            String responseJson = objectMapper.writeValueAsString(response);

            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            log.error("Error processing 'user.get': ", e);
            sendErrorResponse(message, "INTERNAL_ERROR", e.getMessage());
        }
    }


    private void sendErrorResponse(Message message, String code, String msg) {
        try {
            String errorResponse = buildErrorResponse(code, msg);
            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), errorResponse.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            log.error("Error sending error response", ex);
        }
    }

    /**
     * Get All Users - Subject: user.getAll
     * Request: {} (empty)
     * Response: NatsResponse with List<UserDto>
     */
    private void handleGetAllUsers(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.getAll': {}", messageData);
        
        try {
            // Process request
            List<UserDto> users = userService.getAllUsers();
            
            // Build success response
            NatsResponse response = NatsResponse.success(users);
            
            // Serialize and send response
            String responseJson = objectMapper.writeValueAsString(response);
            log.info("Sending response for 'user.getAll': success with {} users", users.size());
            
            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));
            }
            
        } catch (Exception e) {
            log.error("Error processing 'user.getAll': ", e);
            String errorResponse = buildErrorResponse("INTERNAL_ERROR", e.getMessage());
            if (message.getReplyTo() != null) {
                try {
                    natsConnection.publish(message.getReplyTo(), errorResponse.getBytes(StandardCharsets.UTF_8));
                } catch (Exception ex) {
                    log.error("Error sending error response", ex);
                }
            }
        }
    }

    /**
     * Update User - Subject: user.update
     * Request: { "id": Long, "nom": String, "email": String, "age": Integer }
     * Response: NatsResponse with UserDto
     */
    private void handleUpdateUser(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.update': {}", messageData);

        try {
            // Deserialize request
             Map<String, Object> requestMap =
             objectMapper.readValue(messageData, Map.class);

            // Extract data (with fallback)
            Object dataObj = requestMap.get("data");
            if (dataObj == null) {
                dataObj = requestMap;
            }

            if (!(dataObj instanceof Map)) {
                throw new IllegalArgumentException(
                        "Invalid payload structure for user.update: " + requestMap
                );
            }

            Map<String, Object> data = (Map<String, Object>) dataObj;

            // Extract and validate ID
            Object idObj = data.get("id");
            if (idObj == null) {
                throw new IllegalArgumentException("User id is missing in user.update payload");
            }

            Long id;
            try {
                id = Long.valueOf(idObj.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid user id: " + idObj);
            }

            // Build update request
                 UpdateUserRequest request = UpdateUserRequest.builder()
                    .name(data.get("name") != null ? data.get("name").toString() : null)
                    .address(data.get("address") != null ? data.get("address").toString() : null)
                    .email(data.get("email") != null ? data.get("email").toString() : null)
                    .role(data.get("role") != null ? data.get("role").toString() : null)
                    .build();

            // Process request
            UserDto userDto = userService.updateUser(id, request);

            // Build success response
            NatsResponse response = NatsResponse.success(userDto);
            String responseJson = objectMapper.writeValueAsString(response);

            log.info("Sending response for 'user.update': success");

            if (message.getReplyTo() != null) {
                natsConnection.publish(
                        message.getReplyTo(),
                        responseJson.getBytes(StandardCharsets.UTF_8)
                );
            }

        } catch (Exception e) {
            log.error("Error processing 'user.update': ", e);

            String errorResponse =
                    buildErrorResponse("INTERNAL_ERROR", e.getMessage());

            if (message.getReplyTo() != null) {
                try {
                    natsConnection.publish(
                            message.getReplyTo(),
                            errorResponse.getBytes(StandardCharsets.UTF_8)
                    );
                } catch (Exception ex) {
                    log.error("Error sending error response", ex);
                }
            }
        }
    }

    /**
     * Delete User - Subject: user.delete
     * Request: { "id": Long }
     * Response: NatsResponse with null data (204)
     */
    private void handleDeleteUser(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.delete': {}", messageData);

        try {
            Map<String, Object> requestMap = objectMapper.readValue(messageData, Map.class);

            // Search id at the root or in "data"
            Object idObj = requestMap.get("id");
            if (idObj == null && requestMap.get("data") instanceof Map) {
                Map<String,Object> dataMap = (Map<String,Object>) requestMap.get("data");
                idObj = dataMap.get("id");
            }

            if (idObj == null) {
                log.error("Missing 'id' in request: {}", requestMap);
                sendErrorResponse(message, "BAD_REQUEST", "Missing 'id' in request");
                return;
            }

            Long id;
            try {
                id = Long.valueOf(idObj.toString().trim());
            } catch (NumberFormatException e) {
                log.error("Invalid 'id' format in request: {}", idObj);
                sendErrorResponse(message, "BAD_REQUEST", "'id' must be a valid number");
                return;
            }

            // Process request
            userService.deleteUser(id);

            // Build success response with null data
            NatsResponse response = NatsResponse.success(null);
            String responseJson = objectMapper.writeValueAsString(response);
            log.info("Sending response for 'user.delete': success");

            if (message.getReplyTo() != null) {
                natsConnection.publish(message.getReplyTo(), responseJson.getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            log.error("Error processing 'user.delete': ", e);
            sendErrorResponse(message, "INTERNAL_ERROR", e.getMessage());
        }
    }
    /**
     * Notify User - Subject: user.notification
     * Request: { "userId": Long, "message": String, "timestamp": String }
     * Response: NONE (async, no reply)
     */
    private void handleNotifyUser(Message message) {
        String messageData = new String(message.getData(), StandardCharsets.UTF_8);
        log.info("Received NATS message on subject 'user.notification': {}", messageData);
        
        try {
            // Deserialize request
            Map<String, Object> request = objectMapper.readValue(messageData, Map.class);
            Long userId = Long.valueOf(request.get("userId").toString());
            String notificationMessage = request.get("message").toString();
            
            // Process notification (no reply)
            userService.notifyUser(userId, notificationMessage);
            
            log.info("Notification processed for user ID: {}", userId);
            
        } catch (Exception e) {
            log.error("Error processing 'user.notification': ", e);
            // No response needed for async operations
        }
    }

    /**
     * Helper method to build error response
     */
    private String buildErrorResponse(String code, String message) {
        try {
            NatsResponse errorResponse = NatsResponse.error(code, message);
            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            return "{\"success\":false,\"data\":null,\"error\":{\"code\":\"SERIALIZATION_ERROR\",\"message\":\"Failed to serialize error response\"}}";
        }
    }
}