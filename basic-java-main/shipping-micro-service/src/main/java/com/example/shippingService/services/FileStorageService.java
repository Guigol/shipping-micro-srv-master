package com.example.shippingService.services;

import com.example.shippingService.dtos.ProofUploadRequest;
import com.example.shippingService.entities.StoredFile;
import com.example.shippingService.exception.ShipmentNotFoundException;
import com.example.shippingService.repositories.ShipmentRepository;
import com.example.shippingService.repositories.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ShipmentRepository shipmentRepository;
    private final StoredFileRepository storedFileRepository;
    private final CacheManager cacheManager;
    private final GridFsTemplate gridFsTemplate; // For optional persistent storage

    /**
     * Store proof/label file
     * - Redis = cache
     * - Mongo = lasting persistence
     */
    @CachePut(value = "storedFiles", key = "#request.shipmentId + ':' + #request.type")
    public Map<String, Object> storeFile(ProofUploadRequest request) throws IOException {

        // Check if shipment exists
        shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(request.getShipmentId()));

        // 1- Save in Mongo (metadata + Base64)
        StoredFile file = StoredFile.builder()
                .shipmentId(request.getShipmentId())
                .type(request.getType())
                .filename(request.getFilename())
                .contentType(request.getContentType())
                .fileBase64(request.getFileBase64())
                .uploadedAt(Instant.now())
                .build();

        StoredFile saved = storedFileRepository.save(file);

        // 2️- Store in Redis (cache)
        byte[] fileBytes = Base64.decodeBase64(request.getFileBase64());
        cacheManager.getCache("storedFiles")
                .put(request.getShipmentId() + ":" + request.getType(), fileBytes);

        // 3️- Optional persistent storage via GridFS
        gridFsTemplate.store(
                new ByteArrayInputStream(fileBytes),
                request.getShipmentId() + "_" + request.getType(),
                request.getContentType()
        );

        // 4- Reply to the frontend
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("shipmentId", saved.getShipmentId());
        response.put("type", saved.getType());
        response.put("filename", saved.getFilename());
        response.put("contentType", saved.getContentType());
        response.put("fileBase64", saved.getFileBase64());
        response.put("uploadedAt", saved.getUploadedAt());

        log.info("File stored (id={}) in Redis + Mongo + GridFS", saved.getId());
        return response;
    }

    /**
     * Retrieve file from Redis, fallback Mongo if cache missed
     */
    @Cacheable(value = "storedFiles", key = "#shipmentId + ':' + #type")
    public Map<String, Object> getProof(String shipmentId, String type) throws IOException {

        // 1️ Search in Redis
        byte[] cachedFile = (byte[]) cacheManager.getCache("storedFiles")
                .get(shipmentId + ":" + type, byte[].class);

        StoredFile file = storedFileRepository.findByShipmentIdAndType(shipmentId, type);
        if (file == null && cachedFile == null) return null;

        // 2️ If cache is empty, fallback to Mongo
        if (cachedFile == null && file != null) {
            cachedFile = Base64.decodeBase64(file.getFileBase64());
            // fill back Redis cache
            cacheManager.getCache("storedFiles").put(shipmentId + ":" + type, cachedFile);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", file != null ? file.getId() : null);
        data.put("shipmentId", shipmentId);
        data.put("type", type);
        data.put("filename", file != null ? file.getFilename() : null);
        data.put("contentType", file != null ? file.getContentType() : null);
        data.put("fileBase64", Base64.encodeBase64String(cachedFile));
        data.put("uploadedAt", file != null ? file.getUploadedAt() : null);

        return data;
    }

    /**
     * Delete file from Redis + Mongo + GridFS
     */
    @CacheEvict(value = "storedFiles", key = "#shipmentId + ':' + #type")
    public void deleteProof(String shipmentId, String type) throws IOException {

        StoredFile file = storedFileRepository.findByShipmentIdAndType(shipmentId, type);
        if (file != null) {
            storedFileRepository.delete(file);
            log.info("🗑️ File removed from Mongo (id={})", file.getId());
        }

        // Remove from Redis
        cacheManager.getCache("storedFiles").evict(shipmentId + ":" + type);

        // Remove from GridFS
        gridFsTemplate.delete(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("filename")
                                .is(shipmentId + "_" + type)
                )
        );

        log.info("🗑️ File removed from Redis + GridFS (shipmentId={})", shipmentId);
    }
}
