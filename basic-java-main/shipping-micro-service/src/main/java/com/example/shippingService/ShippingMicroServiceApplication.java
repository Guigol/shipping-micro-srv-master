package com.example.shippingService;

import com.example.shippingService.entities.StoredFile;
import com.example.shippingService.repositories.StoredFileRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;



@SpringBootApplication(
		exclude = RedisRepositoriesAutoConfiguration.class
)
@EnableMongoRepositories(basePackages = "com.example.shippingService.repositories")
@Slf4j
public class ShippingMicroServiceApplication {

	@Value("${nats.spring.server}")
	private String natsUrl;

	public static void main(String[] args) {
		SpringApplication.run(ShippingMicroServiceApplication.class, args);
	}

	@PostConstruct
	public void logNatsUrl() {

	@Component
	@RequiredArgsConstructor
	class StoredFileDataLoader implements CommandLineRunner {

		private final StoredFileRepository storedFileRepository;

		@Override
		public void run(String... args) {

			storedFileRepository.deleteAll();
			log.info("🧹 Collection 'stored_files' cleared in DB 'shippingDB'");

			List<StoredFile> files = Arrays.asList(
					createFile("SHIP-1001", "deliveryProof", "proof_1001.pdf", "application/pdf", "JVBERi0xLjMKJcfs..."),
					createFile("SHIP-1002", "label", "label_1002.png", "image/png", "iVBORw0KGgoAAAANSUhEUg..."),
					createFile("SHIP-1003", "invoice", "invoice_1003.pdf", "application/pdf", "JVBERi0xLjMKJcfs..."),
					createFile("SHIP-1004", "depositProof", "depot.txt", "image/png", "VGhpcyBpcyBhIHNhbXBsZSB0ZXh0IGZpbGUu")
			);

			for (StoredFile file : files) {
				storedFileRepository.save(file);
				log.info("✅ StoredFile inserted: {} / {}", file.getShipmentId(), file.getType());
			}
		}

		private StoredFile createFile(
				String shipmentId,
				String type,
				String filename,
				String contentType,
				String fileBase64
		) {
			StoredFile f = new StoredFile();
			f.setShipmentId(shipmentId);
			f.setType(type);
			f.setFilename(filename);
			f.setContentType(contentType);
			f.setFileBase64(fileBase64);
			f.setUploadedAt(Instant.now());
			return f;
			}
		}
	}
}
