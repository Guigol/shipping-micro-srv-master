package com.example.shippingService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class NatsConfig {

    @Value("${nats.spring.server:nats://localhost:4222}")
    private String natsServer;

    @Value("${nats.spring.connection-timeout:5000}")
    private long connectionTimeout;

    @Value("${nats.spring.max-reconnect:10}")
    private int maxReconnect;

    @Value("${nats.spring.reconnect-wait:2000}")
    private long reconnectWait;

    /**
     * Configure NATS Connection
     */
    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
       // log.info("Configuring NATS connection to: {}", natsServer);

        Options options = new Options.Builder()
                .server(natsServer)
                .connectionTimeout(Duration.ofMillis(connectionTimeout))
                .maxReconnects(maxReconnect)
                .reconnectWait(Duration.ofMillis(reconnectWait))
                .errorListener(new ErrorListener() {
                    @Override
                    public void errorOccurred(Connection conn, String error) {
                        log.error("NATS connection error: {}", error);
                    }

                    @Override
                    public void exceptionOccurred(Connection conn, Exception exp) {
                        log.error("NATS connection exception: ", exp);
                    }

                    @Override
                    public void slowConsumerDetected(Connection conn, io.nats.client.Consumer consumer) {
                        log.warn("NATS slow consumer detected");
                    }
                })
                .build();

        Connection connection = Nats.connect(options);
       // log.info("✅ NATS connection established successfully");
        return connection;
    }

    /**
     * Configure ObjectMapper — use Spring Boot’s builder to preserve annotations
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);

        //    log.info("✅ ObjectMapper configured successfully (via Jackson2 builder)");
        };
    }

    /**
     * Expose the configured ObjectMapper bean
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
       // log.info("✅ Custom ObjectMapper bean created via builder");
        return mapper;
    }

}
