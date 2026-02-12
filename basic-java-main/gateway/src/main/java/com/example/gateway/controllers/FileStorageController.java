package com.example.gateway.controllers;

import com.example.gateway.dtos.ProofUploadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class FileStorageController {

    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    // -----------------------------------------------------
    // ✅ POST — Upload Proof (multipart)
    // -----------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{shipmentId}/upload-proof")
    public ResponseEntity<Map<String, Object>> uploadProof(
            @PathVariable String shipmentId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file
    ) {
        try {

            // Convert MultipartFile → DTO + Base64
            ProofUploadRequest req = new ProofUploadRequest();
            req.setShipmentId(shipmentId);
            req.setType(type);
            req.setFilename(file.getOriginalFilename());
            req.setContentType(file.getContentType());
            req.setFileBase64(Base64.getEncoder().encodeToString(file.getBytes()));

            String json = objectMapper.writeValueAsString(req);

            // send to Shipping-service via NATS (request-response)
            CompletableFuture<Message> future = natsConnection.request(
                    "shipping.file.upload",
                    json.getBytes()
            );

            Message replyMsg = future.get();
            Map<String, Object> resp = objectMapper.readValue(replyMsg.getData(), Map.class);

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            log.error("❌ File upload failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // -----------------------------------------------------
    // ✅ GET — Download Proof (bytes from MongoDB)
    // -----------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{shipmentId}/proof/{type}")
    public ResponseEntity<byte[]> downloadProof(
            @PathVariable String shipmentId,
            @PathVariable String type
    ) {
        try {
            // build request
            Map<String, String> request = Map.of(
                    "shipmentId", shipmentId,
                    "type", type
            );

            String json = objectMapper.writeValueAsString(request);

            // send the request to Shipping-service
            CompletableFuture<Message> future = natsConnection.request(
                    "shipping.file.get",
                    json.getBytes()
            );

            Message reply = future.get();
            Map<String, Object> resp = objectMapper.readValue(reply.getData(), Map.class);

            if (!(boolean) resp.getOrDefault("success", false)) {
                return ResponseEntity.status(404).build();
            }

            // Data extraction
            Map<String, Object> file = (Map<String, Object>) resp.get("data");

            String filename = file.get("filename").toString();
            String contentType = file.get("contentType").toString();
            byte[] data = Base64.getDecoder().decode(file.get("fileBase64").toString());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(data);

        } catch (Exception e) {
            log.error("❌ File download failed", e);
            return ResponseEntity.status(500).build();
        }
    }
}
