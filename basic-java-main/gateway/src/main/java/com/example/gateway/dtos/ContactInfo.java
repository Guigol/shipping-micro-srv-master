package com.example.gateway.dtos;

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
}
