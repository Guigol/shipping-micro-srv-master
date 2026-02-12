package com.example.userservice.config;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.entity.User;
import com.example.userservice.entity.UserRole;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final ObjectMapper objectMapper;

    @Bean
    CommandLineRunner loadUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {

            InputStream input = getClass().getClassLoader().getResourceAsStream("users.json");

            if (input == null) {
                System.out.println("⚠ users.json NOT FOUND");
                return;
            }

            List<CreateUserRequest> users =
                    objectMapper.readValue(input, new TypeReference<>() {});

            for (CreateUserRequest dto : users) {

                if (repo.findByEmail(dto.getEmail()).isPresent()) {
                    System.out.println("Skipping existing user: " + dto.getEmail());
                    continue;
                }

                User user = new User();
                user.setName(dto.getName());
                user.setEmail(dto.getEmail());
                user.setAddress(dto.getAddress());
                user.setPassword(encoder.encode(dto.getPassword()));
                user.setRole(String.valueOf(dto.getRole()));

                repo.save(user);

                System.out.println("✔ User imported: " + user.getEmail());
            }

            System.out.println("✔ User JSON import finished.");
        };
    }
}
