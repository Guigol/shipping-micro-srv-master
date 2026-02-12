package com.example.shippingService.listener;

import com.example.shippingService.exception.ErrorDescriptor;
import com.example.shippingService.exception.ExceptionMapper;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class NatsErrorResponseFactory {

    public static Map<String, Object> build(Exception ex) {

        ErrorDescriptor err = ExceptionMapper.map(ex);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "error");
        response.put("source", "Shipping-service");
        response.put("message", err.message());
        response.put("code", err.code());
        response.put("data", null);

        return response;
    }
}

