package com.example.userservice.dto;

import lombok.Data;

@Data
public class UserCreateWrapper {
    private CreateUserRequest data;
    private String userId;
}

