package com.example.shippingService.dtos;

import lombok.Data;

@Data
public class AddTrackingStatusRequest {
    private String status;
    private String location;
    private String note;
    private String trackingNumber;
    private String timestamp;
    private Long userId;
}
