package com.example.userservice.service;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.entity.UserRole;
import com.example.userservice.exception.DuplicateEmailException;
import com.example.userservice.exception.InvalidCredentialsException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user
     */
    public UserDto createUser(@Valid CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new DuplicateEmailException(request.getEmail(), "");
        }
        
        // Create user entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .address(request.getAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(String.valueOf(UserRole.USER))
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return mapToDto(savedUser);
    }
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole())) // string
                .orElse(false);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        log.info("User found: {}", user.getEmail());
        return mapToDto(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Fetching all users");
        
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update user
     */
    public UserDto updateUser(Long id, @Valid UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        // Find existing user
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                throw new DuplicateEmailException(request.getEmail(), "when updating user");
            }
            user.setEmail(request.getEmail());
        }

        // Update other fields
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        // Update role if provided
        if (request.getRole() != null) {
            String role = request.getRole().toUpperCase();
            if (!role.equals("ADMIN") && !role.equals("USER")) {
                throw new IllegalArgumentException("Rôle invalide : " + request.getRole());
            }
            user.setRole(role);
        }


        // Save updated user
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getEmail());

        return mapToDto(updatedUser);
    }

    /**
     * Delete user
     */
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        // Check if user exists
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        
        // Delete user
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Notify user (example async operation - just logs)
     */
    public void notifyUser(Long id, String message) {
        log.info("Notification for user ID {}: {}", id, message);
        // In a real application, this would send an email, push notification, etc.
        // For this example, we just log it
    }

    // ============================================================
    // LOGIN USER (simple auth)
    // Used by NATS: user.login
    // ============================================================
    @Transactional(readOnly = true)
    public UserDto login(String email, String password) {
        log.info("Attempting login for email {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        System.out.println("User ID from DB: " + user.getId());

        // BCrypt password check
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful for {}", email);
        return mapToDto(user);
    }


    /**
     * Map User entity to UserDto
     */
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(LocalDateTime.now())
                .build();
    }



}