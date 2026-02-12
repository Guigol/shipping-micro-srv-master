package com.example.userservice;

import com.example.userservice.entity.User;
import com.example.userservice.entity.UserRole;
import com.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }



    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {

            if (userRepo.findByName("admin").isEmpty()) {

                User admin = new User();
                admin.setName("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setEmail("admin@ship.com");
                admin.setAddress("N/A");
                admin.setRole("ADMIN");

                userRepo.save(admin);
                System.out.println("✔ ADMIN user created: admin / admin123");
            } else {
                System.out.println("✔ ADMIN already exists");
            }
        };
    }
}
