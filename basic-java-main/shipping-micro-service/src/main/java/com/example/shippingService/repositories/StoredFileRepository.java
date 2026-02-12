package com.example.shippingService.repositories;

import com.example.shippingService.entities.StoredFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoredFileRepository extends MongoRepository<StoredFile, String> {

    StoredFile findByShipmentIdAndType(String shipmentId, String type);

    List<StoredFile> findByShipmentId(String shipmentId);
}
