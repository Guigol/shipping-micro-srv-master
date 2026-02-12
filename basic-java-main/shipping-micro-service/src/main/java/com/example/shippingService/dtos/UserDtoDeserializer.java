package com.example.shippingService.dtos;

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

        // Case 1 : "name" is a nested object
        if (node.has("name") && node.get("name").isObject()) {
            JsonNode nameNode = node.get("name");
            user.setId(asText(nameNode.get("id")));
            user.setName(asText(nameNode.get("name")));
            user.setPassword(asText(nameNode.get("password")));
            user.setEmail(asText(nameNode.get("email")));
            user.setAddress(asText(nameNode.get("address")));
            user.setRole(asText(nameNode.get("role")));
        }
        // Case 2 : normal structure
        else {
            user.setId(asText(node.get("id")));
            user.setName(asText(node.get("name")));
            user.setPassword(asText(node.get("password")));
            user.setEmail(asText(node.get("email")));
            user.setAddress(asText(node.get("address")));
            user.setRole(asText(node.get("role")));

            user.setCreatedAt(parseLocalDateTime(node.get("createdAt")));
            user.setUpdatedAt(parseLocalDateTime(node.get("updatedAt")));
        }

        return user;
    }

    private String asText(JsonNode node) {
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}
