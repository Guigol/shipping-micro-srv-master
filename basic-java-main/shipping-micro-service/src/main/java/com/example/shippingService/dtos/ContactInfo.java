package com.example.shippingService.dtos;

import lombok.Data;

@Data
public class ContactInfo {
    private String name;
    private String address;

    public ContactInfo() {}

    public ContactInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Object get(String name) {
        return name;
    }
}
