package com.example.gateway.dtos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class UserDtoDeserializer extends JsonDeserializer<UserDto> {

    private LocalDateTime parseLocalDateTime(JsonNode node) {
        if (node == null || node.isNull()) return null;

        try {
            // ISO case with Z (Instant → LocalDateTime)
            if (node.asText().endsWith("Z")) {
                return Instant.parse(node.asText())
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime();
            }

            // ISO case without Z
            return LocalDateTime.parse(node.asText());

        } catch (Exception e) {
             return null;
        }
    }

    @Override
    public UserDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        UserDto user = new UserDto();

        // ---- NEW : retrieve userId ----
        if (node.has("userId")) {
            JsonNode userIdNode = node.get("userId");
            if (userIdNode != null && !userIdNode.isNull()) {
                user.setUserId(userIdNode.asLong());
            }
        }

        // ---- (Optional) retrieve id if still used for some cases ----
        if (node.has("id")) {
            user.setId(asText(node.get("id")));
        }

        // ---- Retrieve standard fields ----
        user.setName(asText(node.get("name")));
        user.setPassword(asText(node.get("password")));
        user.setEmail(asText(node.get("email")));
        user.setAddress(asText(node.get("address")));
        user.setRole(asText(node.get("role")));

        user.setCreatedAt(parseLocalDateTime(node.get("createdAt")));
        user.setUpdatedAt(parseLocalDateTime(node.get("updatedAt")));

        return user;
    }

    private String asText(JsonNode node) {
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}
