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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RedisMongoIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0").withExposedPorts(6379);

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private RedisTemplate<String, Shipment> redisTemplate;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        mongo.start();
        registry.add("spring.data.mongodb.uri", () -> mongo.getConnectionString() + "/shipping");
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        }

        @Bean
        public RedisTemplate<String, Shipment> redisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Shipment> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);

            // Key = String
            template.setKeySerializer(new StringRedisSerializer());

            // Value = JSON → Shipment
            template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Shipment.class));

            return template;
        }

    }

    @Test
    void testRedisAndMongo() {
        Shipment shipment = Shipment.builder()
                .shipmentId("SHIP-TEST-001")
                .currentStatus("CREATED")
                .build();

        // Mongo
        shipmentRepository.save(shipment);

        Shipment fromMongo = shipmentRepository.findByShipmentId("SHIP-TEST-001")
                .orElseThrow();

        assertThat(fromMongo).isNotNull();

        // Redis
        redisTemplate.opsForValue().set("SHIP-TEST-001", shipment);
        Shipment fromRedis = redisTemplate.opsForValue().get("SHIP-TEST-001");

        assertThat(fromRedis).isNotNull();
        assertThat(fromRedis.getShipmentId()).isEqualTo("SHIP-TEST-001");
    }

}
