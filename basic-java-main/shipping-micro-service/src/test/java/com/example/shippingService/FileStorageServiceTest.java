package com.example.shippingService;

import com.example.shippingService.dtos.ProofUploadRequest;
import com.example.shippingService.entities.Shipment;
import com.example.shippingService.repositories.ShipmentRepository;
import com.example.shippingService.repositories.StoredFileRepository;
import com.example.shippingService.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class FileStorageServiceTest {

    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0.8");

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private StoredFileRepository storedFileRepository;

    private Shipment shipment;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setup() {
        storedFileRepository.deleteAll();
        shipmentRepository.deleteAll();

        shipment = Shipment.builder()
                .shipmentId("SHIP-BASE64-001") // doit être @Id dans l'entité
                .currentStatus("CREATED")
                .build();
        shipmentRepository.save(shipment);
    }

    @Test
    void testStoreFileMongo() throws IOException {
        // Fichier exemple en base64
        String base64Content = "VGhpcyBpcyBhIHRlc3QgZmlsZQ=="; // "This is a test file"

        ProofUploadRequest request = new ProofUploadRequest();
        request.setShipmentId(shipment.getShipmentId());
        request.setType("PROOF");
        request.setFilename("test.txt");
        request.setContentType("text/plain");
        request.setFileBase64(base64Content);

        Map<String, Object> response = fileStorageService.storeFile(request);

        assertThat(response).isNotNull();
        assertThat(response.get("shipmentId")).isEqualTo(shipment.getShipmentId());
        assertThat(response.get("type")).isEqualTo("PROOF");
        assertThat(response.get("fileBase64")).isEqualTo(base64Content);
        assertThat(response.get("filename")).isEqualTo("test.txt");
        assertThat(response.get("contentType")).isEqualTo("text/plain");

        // Vérification dans la base
        var storedFile = storedFileRepository.findByShipmentIdAndType(shipment.getShipmentId(), "PROOF");
        assertThat(storedFile).isNotNull();
        assertThat(storedFile.getFileBase64()).isEqualTo(base64Content);
    }
}
