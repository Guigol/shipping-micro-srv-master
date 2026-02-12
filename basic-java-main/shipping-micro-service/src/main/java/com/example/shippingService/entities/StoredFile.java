package com.example.shippingService.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stored_files")
public class StoredFile {

    @Id
    private String id;

    private String shipmentId;

    private String type;         // Ex: deliveryProof, label, invoice

    private String filename;

    private String contentType;

    private String fileBase64;   // Base64 content

    private Instant uploadedAt;
}
