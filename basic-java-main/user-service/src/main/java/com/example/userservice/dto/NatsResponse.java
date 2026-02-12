package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NatsResponse {
    
    private boolean success;
    private Object data;
    private ErrorDto error;
    
    public static NatsResponse success(Object data) {
        return NatsResponse.builder()
                .success(true)
                .data(data)
                .error(null)
                .build();
    }
    
    public static NatsResponse error(String code, String message) {
        return NatsResponse.builder()
                .success(false)
                .data(null)
                .error(ErrorDto.builder()
                        .code(code)
                        .message(message)
                        .timestamp(java.time.LocalDateTime.now())
                        .build())
                .build();
    }
}