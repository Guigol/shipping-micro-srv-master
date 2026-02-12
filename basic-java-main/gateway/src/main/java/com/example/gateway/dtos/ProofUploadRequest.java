package com.example.gateway.dtos;

import lombok.Data;

@Data
public class ProofUploadRequest {

    private String shipmentId;
    private String type;
    private String filename;
    private String contentType;
    private String fileBase64;  // <== IMPORTANT

}



