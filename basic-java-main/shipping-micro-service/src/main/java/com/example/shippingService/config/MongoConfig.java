package com.example.shippingService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.shippingservice.repository")
public class MongoConfig {
    // Optionnel : Spring Boot se charge souvent de tout si ton application.yml contient la bonne URI.
}
