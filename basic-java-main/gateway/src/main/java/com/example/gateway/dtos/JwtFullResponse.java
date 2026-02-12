package com.example.gateway.dtos;

public record JwtFullResponse(
        String token,
        String email,

       // @JsonProperty("id")
        Long userId,
        String role,
        String name
) {}
