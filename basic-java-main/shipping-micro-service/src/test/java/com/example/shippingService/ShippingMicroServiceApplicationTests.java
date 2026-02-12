package com.example.shippingService;

import com.example.shippingService.entities.Shipment;
import com.example.shippingService.repositories.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

@SpringBootTest
@Testcontainers
class ShippingMicroServiceApplicationTests {

	/* ----------- REDIS ----------- */
	@Container
	public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.0")
			.withExposedPorts(6379);

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	/* ----------- MONGO ----------- */
	@Container
	static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0.5");

	@DynamicPropertySource
	static void registerMongoProperties(DynamicPropertyRegistry registry) {
		mongoContainer.start();

		registry.add("spring.data.mongodb.uri",
				() -> mongoContainer.getConnectionString() + "/shipping");
	}


	/* ----------- Repositories ----------- */
	@Autowired
	private ShipmentRepository shipmentRepository;


	/* ----------- TESTS ----------- */

	@Test
	void testRedis() {
		redisTemplate.opsForValue().set("testKey", "testValue");
		String value = redisTemplate.opsForValue().get("testKey");
		assert value.equals("testValue");
	}

	@Test
	void testMongoCRUD() {
		Shipment shipment = Shipment.builder()
				.shipmentId("SHIP-100")
				.currentStatus("CREATED")
				.build();

		shipmentRepository.save(shipment);

		Optional<Shipment> fromDb = shipmentRepository.findByShipmentId("SHIP-100");
		assert fromDb.isPresent();
		assert fromDb.get().getCurrentStatus().equals("CREATED");
	}

	/* ----------- CONFIG ----------- */

	@TestConfiguration
	static class TestConfig {

		@Bean
		public RedisConnectionFactory redisConnectionFactory() {
			String address = redisContainer.getHost();
			Integer port = redisContainer.getMappedPort(6379);
			return new LettuceConnectionFactory(address, port);
		}

		@Bean
		public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
			RedisTemplate<String, String> template = new RedisTemplate<>();
			template.setConnectionFactory(connectionFactory);
			template.setKeySerializer(new StringRedisSerializer());
			template.setValueSerializer(new StringRedisSerializer());
			return template;
		}
	}
}
