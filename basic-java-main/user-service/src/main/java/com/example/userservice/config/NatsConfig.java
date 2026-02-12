package com.example.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nats.client.Connection;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Configuration
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
        log.info("Configuring NATS connection to: {}", natsServer);
        
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
        log.info("NATS connection established successfully");
        
        return connection;
    }

    /**
     * Configure ObjectMapper for JSON serialization/deserialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        
        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Pretty print JSON (optional, can be disabled in production)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        log.info("ObjectMapper configured successfully");
        
        return mapper;
    }
}