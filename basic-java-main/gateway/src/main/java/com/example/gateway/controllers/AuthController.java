package com.example.gateway.controllers;

import com.example.gateway.dtos.LoginRequest;
import com.example.gateway.dtos.NatsResponse;
import com.example.gateway.dtos.UserDto;
import com.example.gateway.security.JwtUtil;
import com.example.gateway.service.NatsGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String COOKIE_NAME = "AUTH_TOKEN";

    private final NatsGatewayService nats;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    private boolean isDockerProfile() {
        return environment.acceptsProfiles("docker");
    }

    // ===========================
    // LOGIN
    // ===========================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        // 1) Authenticate via NATS
        NatsResponse natsResponse = nats.login(request);
        if (!natsResponse.isSuccess()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // 2) Convert response to UserDto
        UserDto user = new ObjectMapper()
                .convertValue(natsResponse.getData(), UserDto.class);

        // 3) Generate JWT
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getUserId()
        );

        // 4) Set JWT cookie (profile-aware)
        boolean isDocker = isDockerProfile();

        String cookieHeader =
                COOKIE_NAME + "=" + token +
                        "; HttpOnly" +
                        "; Path=/" +
                        "; Max-Age=900" +
                        (isDocker
                                ? "; SameSite=None; Secure"
                                : "; SameSite=Lax");

        response.addHeader("Set-Cookie", cookieHeader);

        // 5) Return user info (NO TOKEN in body)
        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "name", user.getName()
        ));
    }

    // ===========================
    // LOGOUT
    // ===========================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        boolean isDocker = isDockerProfile();

        String cookieHeader =
                COOKIE_NAME + "=;" +
                        "; HttpOnly" +
                        "; Path=/" +
                        "; Max-Age=0" +
                        (isDocker
                                ? "; SameSite=None; Secure"
                                : "; SameSite=Lax");

        response.addHeader("Set-Cookie", cookieHeader);

        return ResponseEntity.ok("Logged out successfully");
    }

    // ===========================
    // CURRENT USER
    // ===========================
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(
            @AuthenticationPrincipal UserDto user
    ) {
        return ResponseEntity.ok(user);
    }
}
